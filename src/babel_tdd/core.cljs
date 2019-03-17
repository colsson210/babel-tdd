(ns babel-tdd.core
  (:require [cljsjs.babylon]
     [oops.core :refer [oget oset!]]
    ))

(defn f [] 1)

(enable-console-print!)

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


(defn scene1 []
  (let [{:keys [engine scene camera light]} (init)
        box (js/BABYLON.Mesh.CreateBox "box" 2 scene)]
    (oset! scene "clearColor" (js/BABYLON.Color3. 0.8 0.8 0.8))
    (.runRenderLoop
      engine
      (fn []
        (.render scene)))
    ))


(scene1)

