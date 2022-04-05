(ns scrap-world.server.api.v1.cmd.core
  (:require
    [compojure.core :as c]
    [ring.middleware.json :refer [wrap-json-response]]
    [ring.util.response :as r]
    [scrap-world.reference.api.v1.cmd :as ref]
    [scrap-world.server.world.generation.primitive-world-generator :as pwg]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    ))


(defn get-all-cmds [req]
  (r/response {:cmds [
                      (str ::pwg/req)
                      ]}))

(def routes
  (list
    (c/GET ref/cmd [] (wrap-json-response get-all-cmds))))