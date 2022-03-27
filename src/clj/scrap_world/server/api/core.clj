(ns scrap-world.server.api.core
  (:require
    [clojure.java.io :as io]
    [clojure.string :as s]
    [clojure.tools.logging :as log]
    [compojure.core :as c]
    [compojure.route :as r]
    [hiccup.page :as h]
    [pandect.algo.md5 :refer [md5]]
    [ring.util.response :as response]
    [scrap-world.server.api.v1.world :as w]
    ))

(def resource-prefix "/res")

(defn generate-resource-name [initial-name & {:keys [root skip-cache]}]
  (if skip-cache
    initial-name
    (let [res-path      (str (or root "public/js") "/" initial-name)
          _             (log/debug "Res path:" res-path)
          resource-file (io/input-stream (io/resource res-path))
          _             (log/debug "Res file:" resource-file)
          hash-str      (md5 resource-file)
          _             (log/debug "resource: " initial-name " md5:" hash-str)
          ]
      (str resource-prefix "/app_" hash-str ".js"))))

(defn get-js-resource [res _]
  (log/debug "Trying to get resources " res)
  (let [res-processed (s/replace res #"_[\d\w]+.js" ".js")
        _             (log/debug "processed resource name" res-processed)
        resp          (assoc-in
                        (response/resource-response (str "public/js/" res-processed))
                        [:headers "Cache-Control"] "max-age=100500")
        _             (log/debug "response " (str resp))]
    resp))

(defn all-routes [cfg]
  (apply c/routes
         (into (list
                 (c/GET "/" [] (fn [_]
                                 {:status  200
                                  :headers {"Content-type" "text/html"}
                                  :body    (h/html5
                                             [:head [:title "Scrap world"]
                                              [:meta {:name    "front-params"
                                                      :content (prn-str (:front cfg))}]]
                                             [:body
                                              [:div#dimmer]
                                              [:div#alerter]
                                              [:div#tooltip]
                                              [:div#modal]
                                              [:div#app]
                                              (h/include-js (generate-resource-name "app.js" :skip-cache (:skip-resource-cache cfg)))])}))
                 (c/GET (str resource-prefix "/:res") [res] (partial get-js-resource res))
                 (r/resources "/public")
                 (r/not-found "Not found"))
               w/routes)

         ))