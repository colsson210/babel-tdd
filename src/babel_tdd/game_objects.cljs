(ns babel-tdd.game-objects)

(defn add-game-obj [objs obj]
  (assoc objs (gensym "game-obj") obj))

(defn init-game-obj [template]
  (merge
    {:position [0 0 0]
     :color    [0 0 0]
     :force    [0 0 0]}
    template))

(defn init-game-objs [objs]
  (reduce
    (fn [acc val]
      (add-game-obj acc (init-game-obj val)))
    {}
    objs))

(defn get-next-game-objs [game-objs]
  (reduce-kv
    (fn [m k v]
      (let [next-v ((get v :update-fn identity) v)]
        (if next-v
          (assoc m k next-v)
          m)))
    {}
    game-objs))

(defn game-objects-render-loop-tick! [game-objects add-game-objects]
  (swap! game-objects get-next-game-objs)
  (doseq [add-game-object add-game-objects]
    (if ((:predicate-fn add-game-object) @game-objects)
      (swap!
        game-objects
        (fn [old-game-objects]
          (add-game-obj old-game-objects ((:create-fn add-game-object) (init-game-obj (:template add-game-object)))))))))

(defn create-get-next-game-objects [initial-game-objects add-game-objects]
  (let [game-objects (atom initial-game-objects)]
    (fn []
      (do
        (game-objects-render-loop-tick! game-objects add-game-objects)
        @game-objects))))