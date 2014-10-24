(ns radix.reload
  (:require [radix.setup :refer [auto-reload?]]
            [ns-tracker.core :refer [ns-tracker]]))

(defn wrap-reload
  "A middleware function to allow code changes to be noticed by a running service (very useful in
  development situations). When auto-reload is enabled, any namespaces of modified files are
  reloaded before the request is passed to the next handler (note: this doesn't work with
  middleware functions).
  Takes the following options:
  :dirs - A list of directories that contain the source files - defaults to [\"src\"]."
  [handler & [options]]
  (if auto-reload?
    (let [source-dirs (:dirs options ["src"])
          modified-namespaces (ns-tracker source-dirs)]
      (fn [request]
        (doseq [ns-sym (modified-namespaces)]
          (require ns-sym :reload))
        (handler request)))
    handler))
