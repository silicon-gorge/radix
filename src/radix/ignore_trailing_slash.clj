(ns radix.ignore-trailing-slash)

(defn- #^String chop
  "Removes the last character of string, does nothing on a zero-length
string."
  [#^String s]
  (let [size (count s)]
    (if (zero? size)
      s
      (subs s 0 (dec (count s))))))

(defn uri-snip-slash
  "Removes a trailing slash from all uris except \"/\"."
  [uri]
  (if (and (.endsWith uri "/")
           (not= "/" uri))
    (chop uri)
    uri))

(defn wrap-ignore-trailing-slash
  "A middleware function to make routes match regardless of whether or not a uri ends in a slash."
  [handler]
  (fn [request]
    (let [rewrite (uri-snip-slash (:uri request))]
      (handler (assoc request :uri rewrite)))))
