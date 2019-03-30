(ns babel-tdd.update-fns)


(def a (partial + 1.1))

(def b (partial * 1.1))

(defn move-right [obj]
  (update obj :x (fn [x] (mod (inc x) 200))))