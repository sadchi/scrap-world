(ns scrap-world.server.api.v1.cmd.core
  (:require
    [compojure.core :as c]
    [ring.middleware.json :refer [wrap-json-response wrap-json-body wrap-json-params]]
    [ring.util.response :as r]
    [scrap-world.reference.api.v1.cmd :as ref]
    ))


(defn get-all-cmds [req]
  (r/response {:ok true}))

(def routes
  (list
    (c/GET ref/cmd [] (wrap-json-response get-all-cmds))))