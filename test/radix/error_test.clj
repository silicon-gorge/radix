(ns radix.error-test
  (:require [environ.core :refer [env]]
            [midje.sweet :refer :all]
            [radix
             [error :refer :all]
             [setup :refer [production?]]]))

(fact-group
 "error-response tests"
 (fact "error response builds json with message and status"
       (error-response "hello" 300) => {:status 300
                                        :headers {"Content-Type" "application/json"}
                                        :body {:message "hello" :status 300 :type "error"}})

 (fact "error response builds json with message, status and type"
       (error-response "hello" 123) => {:status 123
                                        :headers {"Content-Type" "application/json"}
                                        :body {:message "hello" :status 123 :type "error"}})

 (fact "error response builds json with exception"
       (let [response (error-response (Exception. "hello"))
             body (:body response)]
         (:status response) => 500
         (:type body) => "error"
         (:message body) => (contains #"java\.lang\.Exception: hello")))

 (fact "stack traces are hidden when production mode is true"
       (binding [production? true]
         (error-message (Exception. "the message")) => "the message"))

 (fact "stack traces are shown when production mode is false"
       (binding [production? false]
         (error-message (Exception. "the message")) => (contains #"java\.lang\.Exception")))

 (fact "stack traces are shown when production mode is not set"
       (binding [production? nil]
         (error-message (Exception. "the message")) => (contains #"java\.lang\.Exception")))

 (fact "exceptions are caught and logged"
       (let [mock-handler (fn [request] (throw (Exception. "hello")))
             response ((wrap-error-handling mock-handler) {})]
         (:status response) => 500
         (:message (:body response)) => (contains #"java\.lang\.Exception: hello")
         (:id (:body response)) => truthy)))

(fact-group
 "wrap-error-handler tests"
 (fact "successful call passes through normal response"
       (let [mock-handler (fn [request] :response)
             response ((wrap-error-handling mock-handler) {})]
         response => :response))

 (fact "in production mode uncaught exception is trapped and only the message is output"
       (binding [production? true]
         (let [mock-handler (fn [request] (throw (Exception. "An exception")))
               response ((wrap-error-handling mock-handler) {})
               body (:body response)]
           (:status response) => 500
           (:headers response) => {"Content-Type" "application/json"}
           (:id body) => truthy
           (:message body) => "An exception"
           (:status body) => 500
           (:type body) => "error")))

  (fact "in non-production mode uncaught exception is trapped and the stack trace is output"
       (binding [production? false]
         (let [mock-handler (fn [request] (throw (Exception. "An exception")))
               response ((wrap-error-handling mock-handler) {})
               body (:body response)]
           (:status response) => 500
           (:headers response) => {"Content-Type" "application/json"}
           (:id body) => truthy
           (:message body) => (contains "java.lang.Exception: An exception")
           (:status body) => 500
           (:type body) => "error"))))

(fact-group
 "wrap-client-errors handler tests"
 (fact "no exception response when no exception occurs in wrapped handler"
       (let [handler (fn [req] req)
             response ((wrap-client-errors handler) ..request..)]
         response => ..request..))

 (fact "errors of different type are not trapped"
       (let [handler (fn [req] (throw (Exception. "exception")))]
         ((wrap-client-errors handler) {}) => (throws Exception)))

 (fact "error of type badrequest is trapped and response is generated"
       (let [handler (fn [req] (throw-bad-request ..message..))
             response ((wrap-client-errors handler) {})]
         (:status response) => 400
         (:body response) => {:status 400
                              :message ..message..
                              :type "error"}))

 (fact "error of type conflict is trapped and response is generated"
       (let [handler (fn [req] (throw-conflict ..message..))
             response ((wrap-client-errors handler) {})]
         (:status response) => 409
         (:body response) => {:status 409
                              :message ..message..
                              :type "error"}))

 (fact "error of type notfound with no message is trapped and response is generated"
       (let [handler (fn [req] (throw-not-found))
             response ((wrap-client-errors handler) {})]
         (:status response) => 404
         (:body response) => {:status 404
                              :message "Resource not found"
                              :type "error"}))

 (fact "generic error type is trapped and response is generated"
       (let [handler (fn [req] (throw-error-response ..message.. ..status..))
             response ((wrap-client-errors handler) {})]
         (:status response) => ..status..
         (:body response) => {:status ..status..
                              :message ..message..
                              :type "error"}))

 (fact "generic error type supports arbitrary additional properties"
       (let [handler (fn [req] (throw-error-response ..message.. ..status.. {:k "v"}))
             response ((wrap-client-errors handler) {})]
         (:body response) => {:status ..status..
                              :message ..message..
                              :type "error"
                              :k "v"})))
