(ns babel-tdd.core
  (:require-macros [babel-tdd.oops-macros])
  (:require
    [clojure.set]
    [cljsjs.babylon]
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
    (oset! babylon-shape "!force" [(* 0.01 (+ -1.0 (* 2.0 (rand)))) 0.01 0])
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

(def translation {:position [["position" "x"] ["position" "y"] ["position" "z"]]
                  :color    [["material" "emissiveColor" "r"] ["material" "emissiveColor" "g"] ["material" "emissiveColor" "b"]]
                  :force    "force"})

(defn babylon-set-game-object-properties [babylon-obj obj]
  (babel-tdd.oops-macros/oset!-helper
    babylon-obj
    obj
    {:position ["position.x" "position.y" "position.z"]
     :color    ["material.emissiveColor.r" "material.emissiveColor.g" "material.emissiveColor.b"]}))


(defn init-babylon-objs [scene]
  [(create-babylon-shape scene (:box (:objects all-data)))])

(defn init-game-obj [template]
  (merge
    {:position [0 0 0]
     :color    [0 0 0]
     :force    [0 0 0]}
    template))

(defn init-game-objs [objs]
  (reduce
    (fn [acc val]
      (assoc acc (gensym "game-obj") (init-game-obj val)))
    {}
    objs))

(defn keys-diff [map-a map-b]
  (seq (clojure.set/difference (set (keys map-a)) (set (keys map-b)))))

(defn create-babylon-objs [scene game-objs]
  (reduce-kv
    (fn [m k v]
      (assoc m k (create-babylon-shape scene v)))
    {}
    game-objs))

(defn add-babylon-objs [babylon-objs scene game-objs]
  (reduce-kv
    (fn [next-babylon-objs key game-obj]
      (if (contains? next-babylon-objs key)
        next-babylon-objs
        (assoc next-babylon-objs key (create-babylon-shape scene game-obj))))
    babylon-objs
    game-objs))


(defn get-next-game-objs [game-objs]
  (reduce-kv
    (fn [m k v]
      (let [next-v ((get v :update-fn identity) v)]
        (if next-v
          (assoc m k next-v)
          m)))
    {}
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
  ([] (babylon-init "render-canvas"))
  ([canvas-id]
   (let [canvas (.getElementById js/document canvas-id)
         engine (js/BABYLON.Engine. canvas)
         scene (js/BABYLON.Scene. engine)
         camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 0 -10) scene)
         light (js/BABYLON.PointLight. "light" (js/BABYLON.Vector3. 10 10 0) scene)
         action-manager (js/BABYLON.ActionManager. scene)
         keys (atom {})
         game1 (:game1 (:games all-data))
         game-objs (atom (init-game-objs (get game1 :objects-inital-state {})))
         game-objs (atom {})
         babylon-objs (atom {})
         add-fns-pred (:add-fns (:games all-data))
         add-objs (:add-objects (:game1 (:games all-data)))]
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
         (swap! game-objs get-next-game-objs)
         (swap! babylon-objs add-babylon-objs scene @game-objs)
         (let [babylon-objs-to-remove (get-babylon-objs-to-remove @babylon-objs @game-objs)
               keys-to-remove (clojure.core/keys babylon-objs-to-remove)]
           (dispose-babylon-objs babylon-objs-to-remove)
           (swap! babylon-objs dissoc keys-to-remove))
         (set-babylon-objs-properties @babylon-objs @game-objs)
         (doseq [add-obj add-objs]
           (if ((:predicate-fn add-obj) @game-objs)
             (swap!
               game-objs
               (fn [old-game-objs]
                 (assoc
                   old-game-objs
                   (gensym "asdf")
                   ((:create-fn add-obj) (init-game-obj (:template add-obj)))
                   )))
             ))
         ))
     )))

(babylon-init)