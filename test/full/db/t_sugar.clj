(ns full.db.t-sugar
  (:require [full.db.sugar :refer :all]
            [clojure.test :refer :all]))

(deftest assemble-query-test
  (is (= (assemble-query
           [["SELECT * FROM contacts WHERE name = ?" ["foo"]]
            ["AND last_name = ?" ["bar"]]])
         ["SELECT * FROM contacts WHERE name = ? AND last_name = ?"
          ["foo" "bar"]])))

(deftest in-part-test
  (testing "valid arguments"
    (is (= (in-part "id IN (?)" [1 2 3])
           ["id IN (?, ?, ?)" [1 2 3]]))
    (is (= (in-part "id IN (?)" [1 2])
           ["id IN (?, ?)" [1 2]])))
  (testing "invalid arguments"
    (is (thrown? AssertionError
                 (in-part "id IN (?)" [])))
    (is (thrown? AssertionError
                 (in-part "id IN (?)" nil)))))
