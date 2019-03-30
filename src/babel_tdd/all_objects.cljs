(ns babel-tdd.all-objects
  (:require-macros [babel-tdd.read-object])
  (:require [babel-tdd.load-objects]))


(def all-objects
  (->>
    "public/data/objects/all-objects.json"
    babel-tdd.read-object/read-object
    babel-tdd.load-objects/update-fns-to-update-fn))
