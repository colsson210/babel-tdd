(ns babel-tdd.update-fns)


(def a (partial + 1.1))

(def b (partial * 1.1))

(defn color-by-position [obj]
  (let [m255 (partial * 255)
        [x y z] (:position obj)]
    (assoc obj :color (map m255 (:position obj)))))

(defn move-right [obj] (let [[x y z] (:position obj)] {:position [(mod (+ x 0.01) 1.0) y z]}))