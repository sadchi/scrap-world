(ns scrap-world.server.api.core
  (:require
    [clojure.java.io :as io]
    [clojure.string :as s]
    [clojure.tools.logging :as log]
    [hiccup.page :as h]
    [pandect.algo.md5 :refer [md5]]
    [reitit.ring :as ring]
    [reitit.ring.malli]
    [reitit.swagger :as swagger]
    ))

(defn generate-resource-name [initial-name & {:keys [resource-prefix root skip-cache]}]
  (if skip-cache
    initial-name
    (let [res-path (str (or root "public/js") "/" initial-name)
          _ (log/debug "Res path:" res-path)
          resource-file (io/input-stream (io/resource res-path))
          _ (log/debug "Res file:" resource-file)
          hash-str (md5 resource-file)
          _ (log/debug "resource: " initial-name " md5:" hash-str)
          ]
      (str resource-prefix "/app_" hash-str ".js"))))

(defn get-js-resource [req]
  ;(log/debug "Trying to get resources " req)
  (let [
        res (get-in req [:path-params :res])
        res-processed (s/replace res #"_[\d\w]+.js" ".js")
        ]
    (log/debug "processed resource name" res-processed)
    {:status  200
     :headers {"Cache-Control" "max-age=100500"}
     :body    (-> (str "public/js/" res-processed)
                  (io/resource)
                  (io/input-stream))}))

(defn main [cfg]
  [["/" {:get {:no-doc  true
               :handler (fn [_]
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
                                       (h/include-js (generate-resource-name "app.js"
                                                                             :skip-cache (:skip-resource-cache cfg)
                                                                             :resource-prefix "/js"))])})}}]
   ["/js/:res" {:no-doc  true
                :handler (fn [req]
                           (get-js-resource req))}]
   ["/public/*" {:no-doc  true
                 :handler (ring/create-resource-handler)}]
   ["/swagger.json" {:get {:no-doc  true
                           :swagger {:info {:title       "Scrap-world api"
                                            :description "Some description"}
                                     ;:tags [{:name "files", :description "file api"}
                                     ;       {:name "math", :description "math api"}]
                                     ;
                                     }
                           :handler (swagger/create-swagger-handler)}}]])

