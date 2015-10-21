(ns radix.config
  "Helpers for accessing the application's config file, located on the classpath or filesystem"
  (:require [carica
             [core :as carica]
             [map :refer [merge-nested]]
             [middleware :as middleware]]
            [clojure.tools.logging :as log]
            [environ.core :as environ])
  (:import java.net.URL))

(defn config-filepath
  []
  (or (environ/env :app-config-path)
      (format "application-config.edn")))

(defn file-exists?
  [url-filepath]
  (boolean
   (try
     (.openStream url-filepath)
     (catch Exception _))))

(def override-config-fn
  (middleware/env-override-config "ENVIRONMENT" :environments))

(defn prevent-kw-vals
  [m]
  (doseq [[_ v] m]
    (if (map? v)
      (prevent-kw-vals v)
      (when (keyword? v)
        (throw (Exception. "keyword vals are not allowed in config map"))))))

(defn validate-config-middleware
  [f]
  (fn [resources]
    (doto (f resources)
      prevent-kw-vals)))

(defn load-resources-config
  "Loads a config file located on the classpath, with reloading enabled."
  [local-config]
  (carica/configurer local-config [override-config-fn validate-config-middleware]))

(defn load-cached-config
  "Loads and caches config at the filepath specified."
  [filepath]
  (let [url-filepath (URL. (str "file://" filepath))]
    (if (file-exists? url-filepath)
      (carica/configurer url-filepath [carica/cache-config])
      (do (log/info "No config found at location:" filepath)
          (constantly nil)))))

(defn make-config-fn
  "Attempts to find and load a config file on the filesystem or the classpath."
  [filepath]
  (if-let [local-config (carica/resources filepath)]
    (load-resources-config local-config)
    (load-cached-config filepath)))

(def config
  (make-config-fn (config-filepath)))

(defmacro with-env-override
  "Overrides the config function with the supplied environment variable in the lexical scope.

  e.g with this config map:
  
  {:foo 1
   :environments {:integration {:foo 2}}}

  (with-env-override :integration
    (config :foo))

  (config :foo) evaluates to 2 in all threads"
  [environment & body]
  `(with-redefs [config (fn [& args#]
                          (let [config-snapshot# (make-config-fn (config-filepath))]
                            (-> (config-snapshot#)
                                (~'carica.map/merge-nested (config-snapshot# :environments ~environment))
                                (get-in args#))))]
     ~@body))

(defn env
  "Attempts to find kw in the Carica config map. Falls back to environ's `env` fn if none is found."
  ([kw]
   (env kw nil))
  ([kw default]
   (let [config-val (config kw)]
     (if (nil? config-val)
       (environ/env kw default)
       config-val))))
