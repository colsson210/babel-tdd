(ns babel-tdd.add-fns)

(def no-objs empty?)

(defn rand-within [start end]
  (let [r (rand) d (- end start)]
    (+ start (* r d))))


(defn random-position [obj]
  (assoc
    obj
    :position
    [(rand-within -1 1) (rand-within -1 1) (rand-within -1 1)]))

(defn random-force [new-obj]
  (assoc
    new-obj
    :force
    [(* 0.01 (+ -1.0 (* 2.0 (rand)))) 0.01 0]))