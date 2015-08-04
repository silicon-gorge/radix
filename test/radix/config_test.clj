(ns radix.config-test
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [midje.sweet :refer :all]
            [radix.config :refer :all]))

(def ^:private test-config-map
  {:foo 1, :bar "baz" :quux? true})

(fact-group
  "config-tests"
  (fact "app config path env var takes precedence over resource path config"
        (config-filepath) => ..appconfig..
        (provided
         (env :app-config-path) => ..appconfig..
         (format anything anything) => nil :times 0))

  (against-background [(before :contents (do (spit "/tmp/test-config.json" (json/generate-string test-config-map))
                                             (spit "/tmp/test-config.edn" (pr-str test-config-map))))
                       (after :contents (do (io/delete-file "/tmp/test-config.json")
                                            (io/delete-file "/tmp/test-config.edn")))]
    (fact "json and edn app config files are parsed correctly"
          (let [json-config (make-config "/tmp/test-config.json")
                edn-config (make-config "/tmp/test-config.edn")]
            (json-config) => test-config-map
            (edn-config) => test-config-map)))
    
  (fact "empty map is returned if the config isn't found"
        (let [nil-config (make-config nil)
              not-found-config (make-config (str (java.util.UUID/randomUUID)))]
          (nil-config) => {}
          (not-found-config) => {})))
