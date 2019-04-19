(ns babel-tdd.read-object
  (:require [clojure.java.io]
            [clojure.data.json]
            [clojure.string]))

(defn emit-cljs-resolve [v] `(cljs.core/resolve '~(read-string v)))

(defn read-object-fn [obj]
  (if (map? obj)
    (reduce-kv
      (fn [m k v]
        (assoc
          m
          k
          (cond
            (clojure.string/ends-with? (str k) "-fns") (vec (map emit-cljs-resolve v))
            (clojure.string/ends-with? (str k) "-fn") (emit-cljs-resolve v)
            (map? v) (read-object-fn v)
            (coll? v) (map read-object-fn v)
            :else v)))
      {}
      obj)
    (if (and true (coll? obj))
      (map read-object-fn obj)
      obj)
    )
  )

(defmacro inline [path]
  (slurp (clojure.java.io/resource path)))

(defn read-json [json-filename]
  (letfn [(read-json-value-fn [value]
            (cond
              (and (string? value) (clojure.string/ends-with? value ".json")) (read-json value)
              (map? value) (reduce-kv (fn [m k v] (assoc m k (read-json-value-fn v))) {} value)
              (coll? value) (vec (map read-json-value-fn value))
              :else value))]
    (clojure.data.json/read-str
      (slurp (clojure.java.io/resource json-filename))
      :key-fn keyword
      :value-fn (fn [key value] (read-json-value-fn value))))
  )

(defmacro read-object [json-path]
  (read-object-fn (read-json json-path)))

(prn (read-object-fn (read-json "public/data/objects/all-data.json")))