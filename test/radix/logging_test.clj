(ns radix.logging-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [radix.logging :refer :all]))

(deftest wrap-log-details-middleware

  (fact "Middleware sets request attributes into MDC"
        (let [request {:uri "/example"}
              mdc (atom nil)
              handler (fn [req] (reset! mdc (org.slf4j.MDC/getCopyOfContextMap)))
              middleware-fn (wrap-log-details handler)]
          (middleware-fn request)
          @mdc => {"uri" "/example"}))

  (fact "Middleware sets request headers into MDC"
        (let [request {:headers {"x-forwarded-for" "123"}}
              mdc (atom nil)
              handler (fn [req] (reset! mdc (org.slf4j.MDC/getCopyOfContextMap)))
              middleware-fn (wrap-log-details handler)]
          (middleware-fn request)
          @mdc => {"x-forwarded-for" "123"}))

  (fact "Log parameters are cleared after request"
        (let [request {:uri "/example"
                       :headers {"x-forwarded-for" "123"}}
              middleware-fn (wrap-log-details identity)]
          (middleware-fn request)
          (org.slf4j.MDC/getCopyOfContextMap) => nil)))
