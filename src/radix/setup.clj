(ns radix.setup
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [metrics.jvm.core :refer [instrument-jvm]]
            [metrics.reporters.graphite :as graphite])
  (:import [java.io Reader]
           [java.net InetAddress UnknownHostException]
           [java.util Properties]
           [java.util.concurrent TimeUnit]
           [java.util.logging LogManager]
           [org.slf4j.bridge SLF4JBridgeHandler]))

(def ^:dynamic graphite-enabled?
  (Boolean/valueOf (env :graphite-enabled "false")))

(def ^:dynamic graphite-host
  (env :graphite-host))

(def ^:dynamic graphite-port
  (Integer/valueOf (env :graphite-port "2003")))

(def ^:dynamic graphite-post-interval-seconds
  (Integer/valueOf (env :graphite-post-interval-seconds "60")))

(def ^:dynamic graphite-prefix
  (env :graphite-prefix))

(def ^:dynamic service-port
  (Integer/valueOf (env :service-port "8080")))

(def ^:dynamic shutdown-timeout
  (Integer/valueOf (env :shutdown-timeout-millis "5000")))

(def ^:dynamic production?
  (Boolean/valueOf (env :production "false")))

(def ^:dynamic threads
  (Integer/valueOf (env :threads "254")))

(def ^:dynamic auto-reload?
  (Boolean/valueOf (env :auto-reload "false")))

(def graphite-defaults
  {:graphite-enabled? graphite-enabled?
   :graphite-host graphite-host
   :graphite-port graphite-port
   :graphite-post-interval-seconds graphite-post-interval-seconds
   :graphite-prefix graphite-prefix})

(defn read-file-to-properties
  [file-name]
  (with-open [^Reader reader (io/reader file-name)]
    (let [props (Properties.)]
      (.load props reader)
      (into {} (for [[k v] props] [k v])))))

(defn configure-logging
  []
  (.reset (LogManager/getLogManager))
  (SLF4JBridgeHandler/install))

(def hostname
  (try
    (-> (InetAddress/getLocalHost)
        .getHostName
        (str/split #"\.")
        first)
    (catch UnknownHostException e
      "unknown")))

(defn start-graphite-reporting
  [& [options]]
  (instrument-jvm)
  (let [{:keys [graphite-enabled? graphite-host graphite-port
                graphite-post-interval-seconds graphite-prefix]} (merge graphite-defaults options)
        graphite-reporter (graphite/reporter {:host graphite-host
                                              :port graphite-port
                                              :prefix graphite-prefix})]
    (when graphite-enabled?
      (graphite/start graphite-reporter graphite-post-interval-seconds))))

(def version
  (memoize
   (fn [service-name]
     (let [pom-path (format "META-INF/maven/%s/%s/pom.properties" service-name service-name)]
       (if-let [path (.getResource (ClassLoader/getSystemClassLoader) pom-path)]
         ((read-file-to-properties path) "version")
         "development")))))
