(ns babel-tdd.oget-macros)

(defmacro oget-helper [obj-name translations]
  (reduce-kv
    (fn [m k v]
      (assoc m k (vec (map (fn [path] `(~'oget ~obj-name ~path)) v))))
    {}
    translations))
