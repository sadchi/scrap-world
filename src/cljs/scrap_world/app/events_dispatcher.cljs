(ns scrap-world.app.events-dispatcher
  (:require
    [clojure.core.async :as a]
    [scrap-world.app.camera :as ca]
    [scrap-world.app.console :as con]
    [scrap-world.app.events-queue :as eq]
    [scrap-world.common.core :as c]
    [scrap-world.app.world :as w]
    [scrap-world.app.graphics.core :as g]
    ))



(def event-handlers {:console         con/change-console-state
                     :scroll-top      ca/scroll-top
                     :scroll-down     ca/scroll-down
                     :scroll-left     ca/scroll-left
                     :scroll-right    ca/scroll-right
                     :zoom-in         ca/zoom-in
                     :zoom-out        ca/zoom-out
                     :window-resize   ca/window-resize
                     :update-world    w/update-world
                     :viewport-resize g/change-viewport-size
                     :draw-scene      g/draw-scene
                     })

(a/go-loop []
           (let [event (a/<! eq/events-ch)
                 f (get event-handlers (:type event))]
             (c/log "Event fired: " event)
             (when f
               (try
                 (f (:params event))
                 (catch js/Error e
                   (c/log "event-dispatcher error: " e)))))
           (recur))