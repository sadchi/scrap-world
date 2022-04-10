(ns scrap-world.server.core
  (:require
    [malli.util :as mu]
    [muuntaja.core :as m]
    [org.httpkit.server :refer [run-server]]
    [reitit.coercion.malli :as rm]
    [reitit.ring :as ring]
    [reitit.ring.coercion :as coercion]
    [reitit.ring.malli]
    [reitit.ring.middleware.exception :as exception]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [scrap-world.server.api.core :as api]
    ))


(defn app [cfg]
  (ring/ring-handler
    (ring/router
      [(api/main cfg)]
      {:data {:coercion   (rm/create {
                                      :error-keys       #{#_:type :coercion :in :schema :value :errors :humanized #_:transformed}
                                      :compile          mu/open-schema
                                      :strip-extra-keys true
                                      :default-values   true
                                      :options          nil})
              :muuntaja   m/instance
              :middleware [swagger/swagger-feature
                           parameters/parameters-middleware
                           muuntaja/format-negotiate-middleware
                           muuntaja/format-response-middleware
                           exception/exception-middleware
                           muuntaja/format-request-middleware
                           coercion/coerce-response-middleware
                           coercion/coerce-request-middleware]}})
    (ring/routes
      (swagger-ui/create-swagger-ui-handler
        {:path   "/swagger"
         :config {:validatorUrl     nil
                  :operationsSorter "alpha"}})
      (ring/create-default-handler))))


(defn start [cfg]
  (run-server (app cfg) (:server cfg)))


