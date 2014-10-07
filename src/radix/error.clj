(ns radix.error
  (:require [clojure.tools.logging :refer [error]]
            [io.clj.logging :refer [with-logging-context]]
            [radix
             [logging :refer [wrap-log-details]]
             [setup :as setup]])
  (:import [clojure.lang ExceptionInfo]
           [java.io PrintWriter StringWriter]))

(defn error-message
  [^Exception e]
  (or
   (and setup/production?
        (.getMessage e))
   (let [sw (StringWriter.)]
     (.printStackTrace e (PrintWriter. sw))
     (str sw))))

(defn error-response
  ([^Exception e]
     (error-response (error-message e) 500))
  ([message status]
     {:status status
      :headers {"Content-Type" "application/json"}
      :body {:message message
             :status status
             :type "error"}}))

(defn id-error-response
  [^Exception e id]
  (assoc-in (error-response e) [:body :id] id))

(defn wrap-error-handling
  "A middleware function to add catch and log uncaught exceptions, then return a nice json response to the client"
  [handler]
  (wrap-log-details
   (fn [request]
     (let [start (System/currentTimeMillis)]
       (try
         (handler request)
         (catch Throwable e
           (let [request-time (- (System/currentTimeMillis) start)
                 log-id (str (java.util.UUID/randomUUID))]
             (with-logging-context (merge {:request-time request-time :log-id log-id} (ex-data e))
               (error e)
               (id-error-response e log-id)))))))))
