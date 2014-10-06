(ns radix.setup
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [metrics.jvm.core :refer [instrument-jvm]]
            [metrics.reporters.graphite :refer [reporter]])
  (:import [java.io Reader]
           [java.net InetAddress UnknownHostException]
           [java.util Properties]
           [java.util.concurrent TimeUnit]
           [java.util.logging LogManager]
           [org.slf4j.bridge SLF4JBridgeHandler]))

(def graphite-enabled?
  (Boolean/valueOf (env :graphite-enabled "false")))

(def graphite-host
  (env :graphite-host))

(def graphite-port
  (Integer/valueOf (env :graphite-port "2003")))

(def graphite-post-interval
  (Integer/valueOf (env :graphite-post-interval "60")))

(def graphite-post-unit
  (TimeUnit/valueOf (env :graphite-post-unit "SECONDS")))

(def graphite-prefix
  (env :graphite-prefix))

(def service-port
  (Integer/valueOf (env :service-port "8080")))

(def shutdown-timeout
  (Integer/valueOf (env :shutdown-timeout-millis "5000")))

(def production?
  (Boolean/valueOf (env :production "false")))

(def threads
  (Integer/valueOf (env :threads "100")))

(def graphite-defaults
  {:graphite-enabled? graphite-enabled?
   :graphite-host graphite-host
   :graphite-port graphite-port
   :graphite-post-interval graphite-post-interval
   :graphite-post-unit graphite-post-unit
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
  (let [{:keys [graphite-enabled? graphite-host graphite-port graphite-post-interval
                graphite-post-unit graphite-prefix]} (merge graphite-defaults options)
        graphite-reporter (reporter {:host graphite-host
                                     :port graphite-port
                                     :prefix graphite-prefix})]
    (when graphite-enabled?
      (.start graphite-reporter graphite-post-interval graphite-post-unit))))

(defn version
  [service-name]
  (let [pom-path (format "META-INF/maven/%s/%s/pom.properties" service-name service-name)]
    (if-let [path (.getResource (ClassLoader/getSystemClassLoader) pom-path)]
      ((read-file-to-properties path) "version")
      "development")))
