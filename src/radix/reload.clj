(ns radix.reload
  (:require [radix.setup :refer [production?]]
            [ns-tracker.core :refer [ns-tracker]]))

(defn wrap-reload
  "If not in production mode, reload namespaces of modified files before the
  request is passed to the supplied handler.
  Takes the following options:
  :dirs - A list of directories that contain the source files.
  Defaults to [\"src\"]."
  [handler & [options]]
  (if production?
    handler
    (let [source-dirs (:dirs options ["src"])
          modified-namespaces (ns-tracker source-dirs)]
      (fn [request]
        (doseq [ns-sym (modified-namespaces)]
          (require ns-sym :reload))
        (handler request)))))
