(ns radix.error-test
  (:require [clojure.test :refer :all]
            [environ.core :refer [env]]
            [midje.sweet :refer [fact has-prefix =>]]
            [radix
             [error :refer :all]
             [setup :as setup]]))

(deftest test-error-response

  (testing "error response builds json with message and status"
    (is (= (error-response "hello" 300)
           {:status 300
            :headers {"Content-Type" "application/json"}
            :body {:message "hello" :status 300 :type "error"}})))

  (testing "error response builds json with exception"
    (let [response (error-response (Exception. "hello"))
          response-body (:body response)]
      (is (= (:status response)) 500)
      (is (= (:type response-body) "error"))
      (is (re-find #"java\.lang\.Exception: hello" (:message response-body))))))

;; using environ here so we can mock env
(deftest test-error-message

  (binding [setup/production? true]
    (fact "stack traces are hidden when service-production mode is set to true"
          (error-message (Exception. "message only")) => "message only"))

  (binding [setup/production? false]
    (fact "stack traces are shown when service-production mode is not set"
          (error-message (Exception. "message only")) => (has-prefix "java.lang.Exception:"))

    (fact "stack traces are shown when service-production mode is set to false"
          (error-message (Exception. "message only")) => (has-prefix "java.lang.Exception:"))))

(deftest test-error-handling

  (testing "exceptions are caught and logged"
    (let [mock-handler (fn [request] (throw (Exception. "hello")))
          response ((wrap-error-handling mock-handler) {})]
      (is (= (:status response) 500))
      (is (re-find #"java\.lang\.Exception: hello" (:message (:body response))))
      (is (not (nil? (:id (:body response)))))))

  (testing "successful call passes through normal response"
    (let [expected-response {:status 200}
          mock-handler (fn [request] expected-response)
          response ((wrap-error-handling mock-handler) {})]
      (is (= response expected-response)))))
