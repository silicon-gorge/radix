(ns radix.config
  "Helpers for accessing the application's config file, located on the classpath or filesystem"
  (:require [carica.core :as carica]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]])
  (:import java.net.URL))

(defn config-filepath
  []
  (or (env :app-config-path)
      (format "%s-config.edn" (env :service-name "app"))))

(defn file-exists?
  [url-filepath]
  (boolean
   (try
     (.openStream url-filepath)
     (catch Exception _))))

(defn load-resources-config
  "Loads a config file located on the classpath, with reloading enabled."
  [local-config]
  (carica/configurer local-config []))

(defn load-cached-config
  "Loads and caches config at the filepath specified."
  [filepath]
  (let [url-filepath (URL. (str "file://" filepath))]
    (if (file-exists? url-filepath)
      (carica/configurer url-filepath [carica/cache-config])
      (do (log/info "No config found at location:" filepath)
          (constantly {})))))

(defn make-config-fn
  "Attempts to find and load a config file on the filesystem or the classpath."
  [filepath]
  (if-let [local-config (carica/resources filepath)]
    (load-resources-config local-config)
    (load-cached-config filepath)))

(def config
  (make-config-fn (config-filepath)))
