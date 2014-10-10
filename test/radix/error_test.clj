(ns radix.error-test
  (:require [environ.core :refer [env]]
            [midje.sweet :refer :all]
            [radix
             [error :refer :all]
             [setup :refer [production?]]]))

(fact-group
 (fact "error response builds json with message and status"
       (error-response "hello" 300) => {:status 300
                                        :headers {"Content-Type" "application/json"}
                                        :body {:message "hello" :status 300 :type "error"}})

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
         (:id (:body response)) => truthy))

 (fact "successful call passes through normal response"
       (let [mock-handler (fn [request] :response)
             response ((wrap-error-handling mock-handler) {})]
         response => :response)))
