(ns radix.config-test
  (:require [carica.core :as carica]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [environ.core :as environ]
            [midje.sweet :refer :all]
            [radix.config :refer :all])
  (:import java.net.URL))

(fact-group
  "config-tests"
  (fact "app config path env var takes precedence over resource path config"
        (config-filepath) => "app"
        (provided
         (environ/env :app-config-path) => "app"
         (format anything anything) => nil :times 0))

  (against-background [(before :contents (do (spit "/tmp/test-config.json" (json/generate-string (config)))
                                             (spit "/tmp/test-config.edn" (pr-str (config)))))
                       (after :contents (do (io/delete-file "/tmp/test-config.json")
                                            (io/delete-file "/tmp/test-config.edn")))]
    (fact "json and edn app config files are parsed correctly"
          (let [json-config (make-config-fn "/tmp/test-config.json")
                edn-config (make-config-fn "/tmp/test-config.edn")]
            (json-config) => (config)
            (edn-config) => (config))))
    
  (fact "nil is returned if the config isn't found"
        (let [nil-config (make-config-fn nil)
              not-found-config (make-config-fn (str (java.util.UUID/randomUUID)))]
          (nil-config) => nil
          (not-found-config) => nil))

  (fact "env wrapper fn prefers config map values over environ vars"
        (env :foo) => ..configvalue..
        (provided
         (config :foo) => ..configvalue..
         (environ/env :foo nil) => ..envvalue.. :times 0))
  
  (fact "env wrapper fn falls back to environ env vars"
        (env :foo) => ..envvalue..
        (provided
         (config :foo) => nil
         (environ/env :foo nil) => ..envvalue..))
  
  (fact "env wrapper fn defaults to provided value if none are found"
        (env :foo "default") => "default"
        (provided
         (config :foo) => nil
         (environ/env :foo "default") => "default"))

  (fact "with-env-override overrides configs inside lexical scope"
        (config :foo) => 1
        (with-env-override :integration
          (config :foo) => 2)
        (config :foo) => 1))
