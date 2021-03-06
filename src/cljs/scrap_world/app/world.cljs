(ns scrap-world.app.world
  (:require
    [ajax.core :as ajax]
    [scrap-world.common.core :as c]
    [scrap-world.reference.api.core :as rc]
    [scrap-world.reference.api.v1.world :as w]
    [scrap-world.app.events-queue :as eq]
    ))



(defn got-new-world [x]
  (c/log "got new world: " x)
  (eq/dispatch! {:type   :draw-scene
                 :params x}))

(defn failed-new-world [x]
  (c/log "failed to get new world: " x))

(defn update-world [params]
  (c/log "update-world params: " params)
  (ajax/GET w/world {:params params
                     :handler       got-new-world
                     :error-handler failed-new-world}))