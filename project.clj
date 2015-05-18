(defproject mixradio/radix "1.0.11"
  :description "A Clojure library providing the root functionality for web services"
  :license "https://github.com/mixradio/radix/blob/master/LICENSE"
  :url "https://github.com/mixradio/radix"

  :dependencies [[environ "1.0.0"]
                 [io.clj/logging "0.8.1"]
                 [metrics-clojure "2.5.1"]
                 [metrics-clojure-graphite "2.5.1"]
                 [metrics-clojure-jvm "2.5.1"]
                 [metrics-clojure-ring "2.5.1"]
                 [ns-tracker "0.3.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-api "1.7.12"]
                 [org.slf4j/jcl-over-slf4j "1.7.12"]
                 [org.slf4j/jul-to-slf4j "1.7.12"]
                 [org.slf4j/log4j-over-slf4j "1.7.12"]
                 [slingshot "0.12.2"]]

  :profiles {:dev {:dependencies [[ch.qos.logback/logback-classic "1.1.3"]
                                  [midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]
                             [lein-marginalia "0.8.0"]]}
             :provided {:dependencies [[org.clojure/clojure "1.6.0"]]}})
