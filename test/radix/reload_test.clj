(ns radix.reload-test
  (:require [midje.sweet :refer :all]
            [ns-tracker.core :refer [ns-tracker]]
            [radix
             [setup :refer [auto-reload?]]
             [reload :refer :all]]))

(fact-group
 (fact "wrap-reload does nothing when auto-reload is false, just returning the handler passed in"
       (binding [auto-reload? false]
         (wrap-reload :handler) => :handler))

 (fact "wrap-reload does nothing when auto-reload is not set"
       (binding [auto-reload? nil]
         (wrap-reload :handler) => :handler))

 (fact "wrap-reload does nothing when auto-reload is true but no namespaces need to be reloaded"
       (against-background
        (ns-tracker ["src"]) => (fn [] nil))
       (binding [auto-reload? true]
         (let [handler (fn [r] r)
               wrapped-handler (wrap-reload handler)]
           (wrapped-handler :request) => :request)))

 (fact "wrap-reload performs reload when auto-reload is true and namespaces need to be reloaded"
       (against-background
        (ns-tracker ["src"]) => (fn [] #{"ns1" "ns2"})
        (require "ns1" :reload) => nil
        (require "ns2" :reload) => nil)
       (binding [auto-reload? true]
         (let [handler (fn [r] r)
               wrapped-handler (wrap-reload handler)]
           (wrapped-handler :request) => :request))))
