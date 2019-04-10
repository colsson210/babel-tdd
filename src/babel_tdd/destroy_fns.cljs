(ns babel-tdd.destroy-fns)

(defn x-above-1 [obj]
  (if (<= (second (:position obj)) 1)
    obj))