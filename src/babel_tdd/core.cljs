(ns babel-tdd.core
  (:require-macros [cljs.core.async :refer [go]]
                   [babel-tdd.xyz])
  (:require [cljsjs.babylon]
     [oops.core :refer [oget oset!]]
    ))

(prn (babel-tdd.xyz/inline "project.clj"))

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


