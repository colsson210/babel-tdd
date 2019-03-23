(ns babel-tdd.macro)

(defmacro m1 [x] `(+ 1 ~x))


(defn load-object-fn [obj]
  (reduce-kv
    (fn [m k v]
      (assoc
        m
        k
        (cond
          (= k :update-fnx) `(resolve ~(symbol v))
          (= k :update-fn) v
          (map? v) (load-object-fn v)
          :else v)))
    {}
    obj))

(defmacro load-object-m1 [obj] `(load-object-fn ~obj))