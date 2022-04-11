(ns scrap-world.server.world.core)

(def dummy-coord {:x 10
                  :y 15})

(def dummy-object {:type "primitive"
                   :x    (* 15 64)
                   :y    (* 10 64)})

(defn get-world-state-by-window [x y z width length height]
  (let []
   (if (and (<= x (:x dummy-coord) (+ x width))
            (<= y (:y dummy-coord) (+ y height)))
     {:landscape   {}
      :objects     [dummy-object]
      :projectiles {}
      :effects     {}}
     {:landscape   {}
      :objects     []
      :projectiles {}
      :effects     {}})))
