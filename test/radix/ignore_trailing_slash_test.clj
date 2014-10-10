(ns radix.ignore-trailing-slash-test
  (:require [midje.sweet :refer :all]
            [radix.ignore-trailing-slash :refer :all]))

(fact-group
 (fact "Snippet is happy with just slash"
       (uri-snip-slash "/") => "/")

 (fact "Snipper ignores not trailing slash"
       (uri-snip-slash "/thing/thingy") => "/thing/thingy")

 (fact "Snipper removes trailing slash"
       (uri-snip-slash "/thing/thingy/") => "/thing/thingy")

 (fact "Associates new uri in request map"
       (let [handler (fn [r] r)
             snipper (wrap-ignore-trailing-slash handler)]
         (snipper {:uri "/thing/thingy"})) => (contains {:uri "/thing/thingy"})))
