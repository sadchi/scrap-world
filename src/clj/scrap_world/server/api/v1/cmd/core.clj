(ns scrap-world.server.api.v1.cmd.core
  (:require
    [scrap-world.reference.api.v1.cmd :as ref]
    [scrap-world.server.world.generation.primitive-world-generator :as pwg]

    ))


;(defn get-all-cmds [req]
;  (r/response {:cmds [
;                      (str ::pwg/req)
;                      ]}))
;
;(def routes
;  (list
;    (c/GET ref/cmd [] (wrap-json-response get-all-cmds))))