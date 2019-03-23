(ns babel-tdd.read-object
  (:require [clojure.java.io]
            [clojure.data.json]
            [clojure.string]))

(defn load-object [obj]
  (reduce-kv
    (fn [m k v]
      (assoc
        m
        k
        (cond
          (= k :update-fn-z) (str `(resolve (cljs.core/symbol ~v)))
          (= k :update-fn-a) `(cljs.core/resolve 'babel-tdd.update-fns/a)
          (= k :update-fn-b) `(cljs.core/symbol "babel-tdd.update-fns" "a")
          (= k :update-fn) `(cljs.core/resolve '~(read-string v))

          (map? v) (load-object v)
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
      (slurp (clojure.java.io/resource json-filename) )
      :key-fn keyword
      :value-fn value-fn)))

(defmacro inline-stored-object [json-path]
  (load-object (read-json json-path)))

