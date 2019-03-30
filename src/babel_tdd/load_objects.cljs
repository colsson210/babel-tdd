(ns babel-tdd.load-objects
  (:require [babel-tdd.update-fns]))

(defn update-fns-to-update-fn [obj]
  (cond
    (map? obj)
    (if (contains? obj :update-fns)
      (dissoc (assoc obj :update-fn (apply comp (:update-fns obj))) :update-fns)
      (reduce-kv (fn [m k v] (assoc m k (update-fns-to-update-fn v))) {} obj))
    (coll? obj) (map update-fns-to-update-fn obj)
    :else obj))