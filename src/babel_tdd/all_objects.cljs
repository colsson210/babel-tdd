(ns babel-tdd.all-objects
  (:require-macros [babel-tdd.read-object])
  (:require [babel-tdd.load-object]))


(def all-objects
  (->>
    "public/data/objects/all-objects.json"
    babel-tdd.read-object/read-object
    babel-tdd.load-object/load-object))
