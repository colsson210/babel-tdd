(ns babel-tdd.load-object
  (:require [babel-tdd.update-fns]))


(defn load-object [obj]
  (reduce-kv
    (fn [m k v]
      (assoc
        m
        k
        (cond
          (= k :update-fn) `(resolve ~(symbol v))
          (map? v) (load-object v)
          :else v)))
    {}
    obj))

(defn f [] 8)


; (defmacro m-load-object [obj] `(if ~obj 1 2))

; (defmacro m [x] `(if ~x 1 12))

(defn resolve-test [] (resolve 'babel-tdd.update-fns/a))