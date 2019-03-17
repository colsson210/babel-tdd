(ns babel-tdd.xyz
  (:require [clojure.java.io]))

(defmacro inline [path]
  (slurp (clojure.java.io/resource path)))
