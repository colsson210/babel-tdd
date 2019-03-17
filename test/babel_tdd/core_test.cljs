(ns babel-tdd.core-test
  (:require [cljs.test :refer-macros [is testing async]]
            [devcards.core :refer-macros [deftest]]
            [babel-tdd.core]
            [babel-tdd.abc]))

(deftest f-test
  (testing "babel-tdd.core functions"
    (is (= (babel-tdd.core/f) 1))))


(deftest g-test
         (testing "babel-tdd.abc.cljc functions"
           (is (= (babel-tdd.abc/g) 3))))