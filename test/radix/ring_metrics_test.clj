(ns radix.ring-metrics-test
  (:require [clojure.test :refer [deftest testing is]]
            [metrics
             [core :refer [remove-metric]]
             [timers :refer [timer]]]
            [radix.ring-metrics :refer :all])
  (:import [com.codahale.metrics Timer]))

(deftest test-clean-metric-name

  (testing "cleaned metric name has separators removed from start and end"
    (is (= (clean-metric-name ".this.is.a.metric.") "this.is.a.metric")))

  (testing "cleaned metric name has null byte character removed"
    (is (= (clean-metric-name (str "he." (char 0) ".llo.metric")) "he..llo.metric")))

  (testing "cleaned metric name has spaces replaced with dots"
    (is (= (clean-metric-name "hello hello hello") "hello.hello.hello")))

  (testing "cleaned metric name has dot followed by forward slash replaced with dot"
    (is (= (clean-metric-name "./hello./hello./hello./") "hello.hello.hello")))

  (testing "cleaned metric name has forward slash replaced by dot"
    (is (= (clean-metric-name "/hello/hello/hello/") "hello.hello.hello"))))

(deftest test-metric-name

  (testing "metric name includes parts of request and response formatted correctly"
    (let [request {:request-method :get, :uri "/1.x/status"}
          response {:status 201}
          name (metric-name request response [])]
      (is (= (first name) "info"))
      (is (= (second name) "resources"))
      (is (= (nth name 2) "GET.1.x.status.201"))))

  (testing "metric name has guid replaced with 'GUID'"
    (let [request {:request-method :get, :uri "/users/666c6bdc-1ace-4212-84fa-a44c3cf31876"}
          response {:status 404}
          name (metric-name request response [replace-guid])]
      (is (= (nth name 2) "GET.users.GUID.404"))))

  (testing "metric for path with trailing slash does not have double dots"
    (let [request {:request-method :get, :uri "/users/"}
          response {:status 404}
          name (metric-name request response [replace-guid])]
      (is (= (nth name 2) "GET.users.404"))))

  (testing "metric name rewritten with TOKEN replacements"
    (let [request {:request-method :get :uri "/something/WERFGTHY/12345678-1234-1234-1234-123456789abc"}
          response {:status 200}
          name (metric-name request response [[#"[A-Z]{8}" "TOKEN"] replace-guid])]
      (is (= (nth name 2) "GET.something.TOKEN.GUID.200"))))

  (testing "metric name with requests outside app get rewritten to OTHER"
    (let [request {:request-method :get :uri "/abc/xyz"}
          response {:status 200}
          name (metric-name request response [(replace-outside-app "/1.x")])]
      (is (= (nth name 2) "OTHER.200")))
    (let [request {:request-method :get :uri "/1.x/xyz"}
          response {:status 200}
          name (metric-name request response [(replace-outside-app "/1.x")])]
      (is (= (nth name 2) "GET.1.x.xyz.200")))))

(deftest test-metric-update

  (testing "middleware function updates metric when request is received"
    (let [request {:request-method :get, :uri "/1.x/status"}
          response {:status 201}
          mock-handler (fn [request] response)
          name (metric-name request response [])]
      (remove-metric name)
      ((wrap-per-resource-metrics mock-handler [replace-guid replace-mongoid replace-number]) request)
      (is (= (class (timer name)) Timer))
      (is (= (.getCount (timer name)) 1)))))
