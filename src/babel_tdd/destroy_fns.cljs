(ns babel-tdd.destroy-fns)

(defn x-above-1 [obj]
  (if (<= (:x (:position obj)) 1)
    obj))