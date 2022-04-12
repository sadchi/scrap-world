(ns scrap-world.server.server
  (:require
    [clojure.string :as str]
    [clojure.tools.logging :as log]
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
    [scrap-world.server.api.api :as api]
    [scrap-world.server.api.v1.world :as w]
    ))

(defn handler [e request]
  (let [exception-type (str (.getName (.getClass e)))
        exception-msg (.getMessage e)
        trace (->> (.getStackTrace e)
                   (map #(str "  at " %))
                   (map #(str %))
                   doall)]
    (log/error exception-type ":" exception-msg "\n"
               (subs (str/join "\n" trace) 1))
    {:status 500
     :body   {:exception (str e)
              :uri       (:uri request)}}))

(defn app [cfg]
  (ring/ring-handler
    (ring/router
      [(api/main cfg)
       w/routes]
      {
       :data {:coercion   (rm/create {
                                      :error-keys       #{:humanized}
                                      :compile          mu/open-schema
                                      :lite             true
                                      :strip-extra-keys true
                                      :default-values   true
                                      :options          nil})
              :muuntaja   m/instance
              :middleware [swagger/swagger-feature
                           parameters/parameters-middleware
                           muuntaja/format-negotiate-middleware
                           muuntaja/format-response-middleware
                           (exception/create-exception-middleware
                             (merge exception/default-handlers {
                                                                ::exception/default handler
                                                                ::exception/wrap    (fn [handler e request]
                                                                                      (.printStackTrace e)
                                                                                      (handler e request))}))
                           coercion/coerce-exceptions-middleware
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


