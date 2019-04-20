(ns babel-tdd.add-fns)

(def no-objs empty?)


(defn random-force [objs new-obj]
  (assoc
    objs
    (gensym "gobj")
    (assoc
      new-obj
      :force
      [(* 0.01 (+ -1.0 (* 2.0 (rand)))) 0.01 0])))