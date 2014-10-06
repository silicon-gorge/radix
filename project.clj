(defproject mixradio/radix "0.1.0-SNAPSHOT"
  :description "A Clojure library providing the root functionality for web services"
  :url "https://github.com/mixradio/radix"
  :dependencies [[environ "1.0.0"]
                 [metrics-clojure "2.3.0"]
                 [metrics-clojure-graphite "2.3.0"]
                 [metrics-clojure-jvm "2.3.0"]
                 [metrics-clojure-ring "2.3.0"]
                 [org.clojure/clojure "1.6.0"]
                 [org.slf4j/jul-to-slf4j "1.7.7"]]
  :plugins [[lein-release "1.0.5"]]
  :lein-release {:deploy-via :clojars})
