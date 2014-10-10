(ns radix.reload-test
  (:require [midje.sweet :refer :all]
            [ns-tracker.core :refer [ns-tracker]]
            [radix
             [setup :refer [production?]]
             [reload :refer :all]]))

(fact-group
 (fact "wrap-reload does nothing in production mode, just returning the handler passed in"
       (binding [production? true]
         (wrap-reload :handler) => :handler))

 (fact "wrap-reload does nothing in non-production mode when no namespaces need to be reloaded"
       (against-background
        (ns-tracker ["src"]) => (fn [] nil))
       (binding [production? false]
         (let [handler (fn [r] r)
               wrapped-handler (wrap-reload handler)]
           (wrapped-handler :request) => :request)))

 (fact "wrap-reload performs reload in non-production mode when namespaces need to be reloaded"
       (against-background
        (ns-tracker ["src"]) => (fn [] #{"ns1" "ns2"})
        (require "ns1" :reload) => nil
        (require "ns2" :reload) => nil)
       (binding [production? false]
         (let [handler (fn [r] r)
               wrapped-handler (wrap-reload handler)]
           (wrapped-handler :request) => :request))))
