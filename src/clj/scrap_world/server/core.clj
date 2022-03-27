(ns scrap-world.server.core
  (:require
    [org.httpkit.server :refer [run-server]]
    [scrap-world.server.api.core :as api]
    [ring.middleware.reload :refer [wrap-reload]]
    ))


(defn start [cfg]
  (run-server (wrap-reload (api/all-routes cfg)) (:server cfg)))


