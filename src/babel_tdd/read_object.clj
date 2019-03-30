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


(defn read-json [json-filename]
  (let [value-fn (fn [key value]
                   (cond
                     (and (string? value) (clojure.string/ends-with? value ".json")) (read-json value)
                     :else value))]
    (clojure.data.json/read-str
      (slurp (clojure.java.io/resource json-filename))
      :key-fn keyword
      :value-fn value-fn)))

(defmacro read-object [json-path]
  (read-object-fn (read-json json-path)))

