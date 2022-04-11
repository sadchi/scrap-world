(ns scrap-world.server.api.v1.world
  (:require
    [scrap-world.server.world.core :as w]
    [scrap-world.reference.api.v1.world :as ref]
    [reitit.coercion.malli :as cm]
    [clojure.tools.logging :as log]))






(defn- get-world-state-by-window [req]
  (let [{:keys [x y z width length height]} (get-in req [:parameters :querys])]
    (log/debug "get-world-state-by-window req: " req)
    {:status  200
     :headers {"Content-type" "application/json"}
     :body    (w/get-world-state-by-window x y z width length height)}))

;(def routes
;  (list
;    (c/GET ref/world [x y width height] (wrap-json-response (partial
;                                                              get-world-state-by-window
;                                                              (cu/parse-int x)
;                                                              (cu/parse-int y)
;                                                              (cu/parse-int width)
;                                                              (cu/parse-int height))))))

(def routes
  [ref/world {:get {:swagger    {:tags ["inner"]}
                    :coercion   cm/coercion
                    :parameters {:query [:map
                                         [:x nat-int?]
                                         [:y nat-int?]
                                         [:z nat-int?]
                                         [:width nat-int?]
                                         [:length nat-int?]
                                         [:height nat-int?]]}
                    :handler    get-world-state-by-window}}])