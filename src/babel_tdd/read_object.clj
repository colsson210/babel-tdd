(ns babel-tdd.read-object
  (:require [clojure.java.io]
            [clojure.data.json]
            [clojure.string]))

(defn emit-cljs-resolve [v] `(cljs.core/resolve '~(read-string v)))

(defn read-object-fn [obj]
  (reduce-kv
    (fn [m k v]
      (assoc
        m
        k
        (cond
          (= k :update-fns) (vec (map emit-cljs-resolve v))
          (map? v) (read-object-fn v)
          :else v)))
    {}
    obj))

(defmacro inline [path]
  (slurp (clojure.java.io/resource path)))


(defn read-json-value-fn [value]
  (cond
    (and (string? value) (clojure.string/ends-with? value ".json")) (read-json value)
    (coll? value) (vec (map read-json-value-fn value))
    :else value))

(defn read-json [json-filename]
    (clojure.data.json/read-str
      (slurp (clojure.java.io/resource json-filename))
      :key-fn keyword
      :value-fn (fn [key value] (read-json-value-fn value))))

(defmacro read-object [json-path]
  (read-object-fn (read-json json-path)))

