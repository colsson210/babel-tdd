(ns babel-tdd.add-fns)

(def no-objs empty?)

(defn random-force [obj]
  (assoc
    obj
    :force
    [(* 0.01 (+ -1.0 (* 2.0 (rand)))) 0.01 0]))