(ns babel-tdd.load-object
  (:require [babel-tdd.update-fns]
            [babel-tdd.destroy-fns]
            [babel-tdd.add-fns]))

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
      (#(dissoc % :update-fns :predicate-fns :create-fns)
        (merge
          obj
          (if (contains? obj :shape) {:id (keyword (gensym))})
          (if (contains? obj :update-fns) {:update-fn (create-update-fn obj)})
          (if (contains? obj :add-fns) {:add-fns (first (:add-fns obj))})
          (if (contains? obj :predicate-fns) {:predicate-fn (apply every-pred (:predicate-fns obj))})
          (if (contains? obj :create-fns) {:create-fn (apply comp identity (:create-fns obj))})
          )))
    (coll? obj) (map load-object obj)
    :else obj))