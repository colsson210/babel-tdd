(ns babel-tdd.core
  (:require-macros [babel-tdd.oget-macros])
  (:require [cljsjs.babylon]
            [oops.core :refer [oget oset!]]
            [babel-tdd.all-objects :refer [all-objects]]))

(enable-console-print!)

(defn msg [& txt]
  (oset! (.getElementById js/document "msg") "textContent" (apply str txt)))

(msg all-objects)


(defn create-material [scene obj r g b]
  (let [material (js/BABYLON.StandardMaterial. "material" scene)]
    (oset! material "emissiveColor" (js/BABYLON.Color3. r g b))
    (oset! obj "material" material)))

(defn create-babylon-shape [scene obj]
  (let [babylon-shape (cond
                        (= (:shape obj) "box") (js/BABYLON.MeshBuilder.CreateBox (str (gensym "box")) {:size 3} scene)
                        (= (:shape obj) "circle") (js/BABYLON.MeshBuilder.CreateSphere (str (gensym "circle")) {:diameter 1} scene)
                        (= (:shape obj) "line") (js/BABYLON.MeshBuilder.CreateLines
                                                    (str (gensym "line"))
                                                    (clj->js {:points [(js/BABYLON.Vector3. 0 0 0) (js/BABYLON.Vector3. 1 1 1)]})
                                                    scene))]
    (create-material scene babylon-shape 1.0 1.0 1.0)
    (oset! babylon-shape "!update-fn" (:update-fn obj))
    babylon-shape))

(defn ensure-has-babylon-shape [scene obj]
  (if (and (contains? obj :shape) (not (contains? obj :babylon)))
    (assoc obj :babylon (create-babylon-shape scene obj))
    obj))

(defn create-babylon-shapes [scene objects]
  (letfn [(helper [current-object]
            (cond
              (map? current-object)
              (reduce-kv (fn [m k v] (assoc m k (helper v)))
                         (if (contains? current-object :shape)
                           {:babylon (create-babylon-shape scene current-object)}
                           ; {}
                           {})
                         current-object)
              ; (coll? current-object) (map helper current-object)
              :else current-object))]
    (helper objects)))

  ; some position changing functions should consider fps and some shouldnt
  ; therefore the fps should be an argument to the update-fn


; Movement and collision handling should be done by babylon
; babylon is/has a physics engine
; mesh.moveCollision
; mesh.onCollide = function(collidedMesh) {




(defn babylon-get-obj [babylon-obj]
  (babel-tdd.oget-macros/oget-helper
    babylon-obj
    {:position ["position.x" "position.y" "position.z"]
:color ["material.emissiveColor.r" "material.emissiveColor.g" "material.emissiveColor.b"]}))



(defn babylon-update-obj [babylon-obj obj]
  (let [[x y z] (:position obj)
        [r g b] (:color obj)]
    (oset! babylon-obj "position.x" x)
    (oset! babylon-obj "position.y" y)
    (oset! babylon-obj "position.z" z)
    (oset! babylon-obj "material.emissiveColor.r" r)
    (oset! babylon-obj "material.emissiveColor.g" g)
    (oset! babylon-obj "material.emissiveColor.b" b)
    ))

(defn babylon-update [babylon-objs]
  (doseq [babylon-obj babylon-objs]
    (let [update-fn (oget babylon-obj "update-fn")
          obj (babylon-get-obj babylon-obj)]
      (babylon-update-obj babylon-obj (update-fn obj)))))

(defn babylon-init
  ([] (babylon-init "render-canvas"))
  ([canvas-id]
   (let [canvas (.getElementById js/document canvas-id)
         engine (js/BABYLON.Engine. canvas)
         scene (js/BABYLON.Scene. engine)
         camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 0 -10) scene)
         light (js/BABYLON.PointLight. "light" (js/BABYLON.Vector3. 10 10 0) scene)
         action-manager (js/BABYLON.ActionManager. scene)
         keys (atom {})
         box (create-babylon-shape scene (:box all-objects))
         babylon-objs [box]]
     (oset! scene "!actionManager" action-manager)
     (oset! scene "clearColor" (js/BABYLON.Color3. 0.8 0.8 0.0))
     (.render scene)
     (.registerAction
       action-manager
       (js/BABYLON.ExecuteCodeAction.
         js/BABYLON.ActionManager.OnKeyDownTrigger
         (fn [event] (swap! keys assoc (oget event "sourceEvent.key") true))))
     (.registerAction
       action-manager
       (js/BABYLON.ExecuteCodeAction.
         js/BABYLON.ActionManager.OnKeyUpTrigger
         (fn [event] (swap! keys dissoc (oget event "sourceEvent.key")))))
     (.runRenderLoop engine (fn [] (babylon-update babylon-objs) (.render scene)))
     )))

(babylon-init)