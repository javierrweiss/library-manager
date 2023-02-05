(ns javierweiss.library-manager.core-test
  (:require
    [javierweiss.library-manager.test-utils :as utils]
    [clojure.test :refer :all]))

(use-fixtures :once (utils/system-fixture))

(deftest example-test 
  (is (= 1 1)))


(comment 
  (utils/system-state)
  )
