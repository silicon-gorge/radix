(ns radix.logging
  (:require [io.clj.logging :refer [with-logging-context]]))

(def ^:const logged-request-attributes #{:request-method :uri :query-string :server-name})

(def ^:const logged-headers #{"x-forwarded-for" "user-agent"})

(defn wrap-log-details
  "A middleware function to add useful request details to the logging context"
  [handler]
  (fn [request]
    (with-logging-context (merge (select-keys request logged-request-attributes)
                                 (select-keys (:headers request) logged-headers))
      (handler request))))
