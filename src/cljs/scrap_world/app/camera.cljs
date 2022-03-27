(ns scrap-world.app.camera
  (:require
    [scrap-world.common.core :as c]
    [scrap-world.app.events-queue :as eq]))


(def camera-params (atom nil))

(defn init-camera-params! [x]
  (let [position-inited (assoc x :position
                                 (merge {:x 0 :y 0 :zoom 1.0} (:position x)))]
    (c/log "init-camera-params! x: " x)
    (c/log "init-camera-params! position-inited: " position-inited)
    (reset! camera-params position-inited)))


(defn update-world! []
  (let [c-params @camera-params
        tile (get-in c-params [:tile :size])
        zoom (get-in c-params [:position :zoom])]
    (eq/dispatch! {:type   :update-world
                   :params {:x      (get-in c-params [:position :x])
                            :y      (get-in c-params [:position :y])
                            :width  (-> (get-in c-params [:size-px :width])
                                        (/ tile)
                                        (/ zoom)
                                        (Math/round))
                            :height (-> (get-in c-params [:size-px :height])
                                        (/ tile)
                                        (/ zoom)
                                        (Math/round))}})))

(defn update-camera-position! [x-delta y-delta zoom-delta]
  (c/log "new camera params: " (swap! camera-params (fn [x]
                                                      (-> x
                                                          (update-in [:position :x] + x-delta)
                                                          (update-in [:position :y] + y-delta)
                                                          (update-in [:position :zoom] #(max (:zoom-step x) (+ % zoom-delta)))))))
  (update-world!))

(defn scroll-top [x]
  (update-camera-position! 0 (- (:scroll-step @camera-params)) 0))

(defn scroll-down [x]
  (update-camera-position! 0 (:scroll-step @camera-params) 0))

(defn scroll-left [x]
  (update-camera-position! (- (:scroll-step @camera-params)) 0 0))

(defn scroll-right [x]
  (update-camera-position! (:scroll-step @camera-params) 0 0))

(defn zoom-in [x]
  (update-camera-position! 0 0 (:zoom-step @camera-params)))

(defn zoom-out [x]
  (update-camera-position! 0 0 (- (:zoom-step @camera-params))))

(defn window-resize [{:keys [width height] :as params}]
  (c/log "new camera params: " (swap! camera-params assoc-in [:size-px] params))
  (eq/dispatch! {:type   :viewport-resize
                 :params params})
  (update-world!))
