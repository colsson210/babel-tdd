(ns babel-tdd.core
  (:require
    [oops.core :refer [oget oset!]]
    [babel-tdd.all-data :refer [all-data]]
    [babel-tdd.game-objects]
    [babel-tdd.babylon-helpers]))

(enable-console-print!)

(defn msg [& txt]
  (oset! (.getElementById js/document "msg") "textContent" (apply str txt)))

(msg all-data)


(def default-render-canvas-id "render-canvas")

(babel-tdd.babylon-helpers/babylon-init
  default-render-canvas-id
  (babel-tdd.game-objects/create-get-next-game-objects {} (:add-objects (:game1 (:games all-data))))
  )