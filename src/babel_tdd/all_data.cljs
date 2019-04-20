(ns babel-tdd.all-data
  (:require-macros [babel-tdd.read-object])
  (:require [babel-tdd.load-object]))


(def a
  (babel-tdd.read-object/read-object "public/data/objects/all-data.json"))

(def all-data
  (->>
    "public/data/objects/all-data.json"
    babel-tdd.read-object/read-object
    babel-tdd.load-object/load-object))
