(ns babel-tdd.core
  (:require-macros [babel-tdd.oops-macros])
  (:require [cljsjs.babylon]
            [oops.core :refer [oget oset!]]
            [babel-tdd.all-data :refer [all-data]]))

(enable-console-print!)

(defn msg [& txt]
  (oset! (.getElementById js/document "msg") "textContent" (apply str txt)))

(msg all-data)


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
              (coll? current-object) (map helper current-object)
              :else current-object))]
    (helper objects)))

; some position changing functions should consider fps and some shouldn't
; therefore the fps should be an argument to the update-fn


; Movement and collision handling should be done by babylon
; babylon is/has a physics engine
; mesh.moveCollision
; mesh.onCollide = function(collidedMesh) {


(defn babylon-get-game-object [babylon-obj]
  (babel-tdd.oops-macros/oget-helper
    babylon-obj
    {:position ["position.x" "position.y" "position.z"]
     :color    ["material.emissiveColor.r" "material.emissiveColor.g" "material.emissiveColor.b"]}))

(defn babylon-set-game-object-properties [babylon-obj obj]
  (babel-tdd.oops-macros/oset!-helper
    babylon-obj
    obj
    {:position ["position.x" "position.y" "position.z"]
    :color    ["material.emissiveColor.r" "material.emissiveColor.g" "material.emissiveColor.b"]}))


(defn get-next-game-objects [babylon-objs]
  (map
    (fn [babylon-obj]
      (let [update-fn (oget babylon-obj "update-fn")
            game-object (babylon-get-game-object babylon-obj)]
        (update-fn game-object)))
    babylon-objs))


(defn babylon-update [babylon-objs]
  (let [next-game-objects (get-next-game-objects babylon-objs)
        next-pairs
        ((comp (partial filter first) (partial map list)) next-game-objects babylon-objs)]
    (doseq [next-pair next-pairs]
        (babylon-set-game-object-properties (second next-pair) (first next-pair)))
    (map second next-pairs)))

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
         box (create-babylon-shape scene (:box (:objects all-data)))
         babylon-objs [box]]
     (oset! scene "!actionManager" action-manager)
     (oset! scene "clearColor" (js/BABYLON.Color3. 0.8 0.8 0.8))
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