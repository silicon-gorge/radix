(ns radix.ignore-trailing-slash-test
  (:require [clojure.test :refer [deftest testing is]]
            [radix.ignore-trailing-slash :refer :all]))

(deftest test-uri-snipping

  (testing "Snippet is happy with just slash"
    (let [uri "/"
          snipped (uri-snip-slash uri)]
      (is (= uri snipped))))

  (testing "Snipper ignores non trailing slash"
    (let [uri "/thing/thingy"
          snipped (uri-snip-slash uri)]
      (is (= uri snipped))))

  (testing "Snipper removes trailing slash"
    (let [uri "/thing/thingy/"
          snipped (uri-snip-slash uri)]
      (is (not= uri snipped))
      (is (= "/thing/thingy" snipped))))

  (testing "Associates new uri in request map"
    (let [handler (fn [r] r)
          request {:uri "/thing/thingy/"}
          snipper (wrap-ignore-trailing-slash handler)
          snipped (snipper request)]
      (is (= "/thing/thingy" (:uri snipped))))))
