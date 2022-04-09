(ns scrap-world.server.api.v1.world
  (:require
    [scrap-world.server.world.core :as w]
    [scrap-world.common-utils :as cu]
    [scrap-world.reference.api.v1.world :as ref]
    ))



;(defn- get-world-state-by-window [x y width height _]
;  (r/response (w/get-world-state-by-window x y width height)))
;
;(def routes
;  (list
;    (c/GET ref/world [x y width height] (wrap-json-response (partial
;                                                              get-world-state-by-window
;                                                              (cu/parse-int x)
;                                                              (cu/parse-int y)
;                                                              (cu/parse-int width)
;                                                              (cu/parse-int height))))))
