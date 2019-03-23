(ns babel-tdd.core
  (:require-macros [cljs.core.async :refer [go]]
                   [babel-tdd.read-object]
                   [babel-tdd.macro :refer [m1 load-object-m1]])
  (:require [cljsjs.babylon]
     [oops.core :refer [oget oset!]]
            [babel-tdd.load-object :refer [load-object resolve-test]]
            [babel-tdd.update-fns]
    ))


(defn ftest [] 123)

(defn msg [txt]
  (oset! (.getElementById js/document "msg") "textContent" txt))

;(def inlined-object (macroexpand-1 '(babel-tdd.read-object/inline-stored-object "public/data/objects/line.json")) )
(def inlined-object (babel-tdd.read-object/inline-stored-object "public/data/objects/line.json"))

; (def inline2 (babel-tdd.read-object/inline-stored-object "public/data/objects/cave-segment.json"))

(prn "inlined-object:" inlined-object)

(prn "inlined-object call upd-fn:" ((:update-fn inlined-object)))


;(def inline3 (load-object inlined-object))
;(prn "load-object:" inline3)

;(prn "load-object-m1" (load-object-m1 inlined-object))

;(def asdf (resolve (first (:update-fns inline3))))
; (prn "asdf:" asdf)
(msg "asd")

(prn "resolve-test:" (resolve-test))


(prn "m1 test:" (m1 2))

(def line-string (babel-tdd.read-object/inline "public/data/objects/line.json"))
(prn (js->clj (.parse js/JSON line-string) :keywordize-keys true ))

(defn f [] 1)

(enable-console-print!)


(defn set-color [scene obj r g b]
  (let [material (js/BABYLON.StandardMaterial. "material" scene)]
    (oset! material "emissiveColor" (js/BABYLON.Color3. r g b))
    (oset! obj "material" material)))

(defn init
  ([] (init "render-canvas"))
  ([canvas-id]
    (let [canvas (.getElementById js/document canvas-id)
           engine (js/BABYLON.Engine. canvas)
           scene (js/BABYLON.Scene. engine)
           camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 0 -10) scene)
           light (js/BABYLON.PointLight. "light" (js/BABYLON.Vector3. 10 10 0) scene)
           action-manager (js/BABYLON.ActionManager. scene)]
       (oset! scene "!actionManager" action-manager)
      {:engine engine :scene scene :camera camera :light light})))


(defn set-keys [scene keys-fn-map]
  (.registerAction
    (oget scene "actionManager")
    (js/BABYLON.ExecuteCodeAction.
      js/BABYLON.ActionManager.OnKeyDownTrigger
      (fn [event]
        (let [key-fn (keys-fn-map (oget event "sourceEvent.key"))]
          (if (fn? key-fn) (key-fn)))))))

(defn scene1 []
  (let [{:keys [engine scene camera light]} (init)
        box (js/BABYLON.Mesh.CreateBox "box" 2 scene)]
    (oset! scene "clearColor" (js/BABYLON.Color3. 0.8 0.8 0.8))
    (set-color scene box 0.8 0.1 0.1)
    (set-keys
      scene
      { "ArrowUp" (fn [] (oset! box "position.y" (+ (oget box "position.y") 0.1)))
       "ArrowDown" (fn [] (oset! box "position.y" (- (oget box "position.y") 0.1)))})
    (.runRenderLoop
      engine
      (fn []
        ; (.getFps engine)
        (.render scene)
        (oset! box "rotation.y" (+ (oget box "rotation.y") 0.01))
        (oset! box "rotation.z" (+ (oget box "rotation.z") 0.01))))
    ))

(scene1)


