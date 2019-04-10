(ns babel-tdd.load-object
  (:require [babel-tdd.update-fns]
            [babel-tdd.destroy-fns]))

(defn call-if [f]
  (fn [x] (if x (f x))))

(defn create-update-fn [obj]
  (apply
    comp
    (map call-if (:update-fns obj))))

(defn load-object [obj]
  (cond
    (map? obj)
    (reduce-kv
      (fn [m k v] (assoc m k (load-object v)))
      {}
      (#(dissoc % :update-fns)
        (merge
          obj
          (if (contains? obj :shape) {:id (keyword (gensym))})
          (if (contains? obj :update-fns) {:update-fn (create-update-fn obj)}))))
    (coll? obj) (map load-object obj)
    :else obj))