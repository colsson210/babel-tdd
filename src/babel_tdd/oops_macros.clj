(ns babel-tdd.oops-macros)

(defmacro oget-helper [babylon-obj translations]
  (reduce-kv
    (fn [m k v]
      (assoc m k (vec (map (fn [path] `(~'oget ~babylon-obj ~path)) v))))
    {}
    translations))

(defmacro oset!-helper [babylon-obj obj translations]
  (reduce-kv
    (fn [oset!-exprs k v]
      (concat
        oset!-exprs
        (map-indexed (fn [index path] `(~'oset! ~babylon-obj ~path (~'nth (~k ~obj) ~index))) v)))
    ['do]
    translations))