(ns babel-tdd.babylon-helpers
  (:require-macros [babel-tdd.oops-macros])
  (:require
    [clojure.set]
    [cljsjs.babylon]
    [oops.core :refer [oget oset!]]))

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
    (oset! babylon-shape "!force" [(* 0.01 (+ -1.0 (* 2.0 (rand)))) 0.01 0])
    babylon-shape))

; some position changing functions should consider fps and some shouldn't
; therefore the fps should be an argument to the update-fn


; Movement and collision handling should be done by babylon
; babylon is/has a physics engine
; mesh.moveCollision
; mesh.onCollide = function(collidedMesh) {

(defn keys-diff [map-a map-b]
  (seq (clojure.set/difference (set (keys map-a)) (set (keys map-b)))))

(defn add-babylon-objs [babylon-objs scene game-objs]
  (reduce-kv
    (fn [next-babylon-objs key game-obj]
      (if (contains? next-babylon-objs key)
        next-babylon-objs
        (assoc next-babylon-objs key (create-babylon-shape scene game-obj))))
    babylon-objs
    game-objs))

(defn get-babylon-objs-to-remove [babylon-objs game-objs]
  (reduce-kv
    (fn [m k v]
      (if (not (contains? game-objs k))
        (assoc m k v)
        m))
    {}
    babylon-objs))

(defn dispose-babylon-objs [babylon-objs]
  (doseq [babylon-obj (vals babylon-objs)]
    (.dispose babylon-obj)))

(defn set-babylon-objs-properties [babylon-objs game-objs]
  (doseq [[k game-obj] game-objs]
    (if (contains? babylon-objs k)
      (let [babylon-obj (babylon-objs k)
            [x y z] (:position game-obj)
            [r g b] (:color game-obj)]
        (oset! babylon-obj "position.x" x)
        (oset! babylon-obj "position.y" y)
        (oset! babylon-obj "position.z" z)
        (oset! babylon-obj "material.emissiveColor.r" r)
        (oset! babylon-obj "material.emissiveColor.g" g)
        (oset! babylon-obj "material.emissiveColor.b" b)))))

(defn babylon-init
  ([canvas-id get-next-game-objects]
   (let [canvas (.getElementById js/document canvas-id)
         engine (js/BABYLON.Engine. canvas)
         scene (js/BABYLON.Scene. engine)
         camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 0 -10) scene)
         light (js/BABYLON.PointLight. "light" (js/BABYLON.Vector3. 10 10 0) scene)
         action-manager (js/BABYLON.ActionManager. scene)
         keys (atom {})
         babylon-objs (atom {})
         ]
     (oset! scene "!actionManager" action-manager)
     (oset! scene "clearColor" (js/BABYLON.Color3. 0.8 0.8 0.8))
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
     (.runRenderLoop
       engine
       (fn []
         (.render scene)
         (let [game-objs (get-next-game-objects)]
           (swap! babylon-objs add-babylon-objs scene game-objs)
           (let [babylon-objs-to-remove (get-babylon-objs-to-remove @babylon-objs game-objs)
                 keys-to-remove (clojure.core/keys babylon-objs-to-remove)]
             (dispose-babylon-objs babylon-objs-to-remove)
             (swap! babylon-objs dissoc keys-to-remove))
           (set-babylon-objs-properties @babylon-objs game-objs)
           )
         ))
     )))