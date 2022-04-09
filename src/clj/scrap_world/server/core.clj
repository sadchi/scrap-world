(ns scrap-world.server.core
  (:require
    [org.httpkit.server :refer [run-server]]
    [scrap-world.server.api.core :as api]
    ))


(defn start [cfg]
  (run-server (api/all-routes cfg) (:server cfg)))


