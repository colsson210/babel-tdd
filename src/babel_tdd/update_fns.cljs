(ns babel-tdd.update-fns)


(def a (partial + 1.1))

(def b (partial * 1.1))

(defn color-by-position [obj]
  (let [[x y z] (:position obj)]
    (assoc
      obj
      :color
      [x y z])))

(defn move-right [obj]
  (let [[x y z] (:position obj)]
    (assoc
      obj
      :position
      [(mod (+ x 0.01) 1.0) y z]
      )))

(defn move-up [obj]
  (let [[x y z] (:position obj)]
    (assoc
      obj
      :position
      [x (mod (+ y 0.01) 1.0) z]
      )))