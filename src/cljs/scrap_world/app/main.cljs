(ns scrap-world.app.main
  (:require
    [cljs.reader :as reader]
    [reagent.core :as r]
    [reagent.dom :as rd]
    [scrap-world.app.camera :as camera]
    [scrap-world.app.inputs :as i]
    [scrap-world.common.core :as c]
    [scrap-world.app.console :as cn]
    [scrap-world.common.events-tools :as et]
    [scrap-world.app.events-queue :as eq]
    [scrap-world.app.events-dispatcher :as ed]
    [scrap-world.app.graphics.core :as g]

    ))


(def resize-timeout 1000)



(defn reload! [] (println "Code updated."))

(defn main! [] (println "App loaded!"))


(defn get-front-params []
  (let [metas (.getElementsByTagName js/document "meta")
        front-params (-> (reduce (fn [_ i]
                                   (let [imeta (aget metas i)]
                                     (when (= "front-params" (.-name imeta))
                                       (reduced (.-content imeta))))) nil (range (.-length metas)))
                         str
                         (reader/read-string))]
    (c/log "get-front-params front-params: " front-params)
    front-params))

(defn app []
  (r/create-class
    {:component-did-mount (fn [comp]
                            (let [app-node (rd/dom-node comp)

                                  canvas (js/document.getElementById "main-canvas")

                                  update-size (fn []
                                                (let [r (.getBoundingClientRect app-node)]
                                                  (c/log "update-size rect: " (.-width r) " " (.-height r))
                                                  (set! (.-width canvas) (.-width r))
                                                  (set! (.-height canvas) (.-height r))
                                                  (eq/dispatch! {:type   :window-resize
                                                                 :params {:width  (.-width r)
                                                                          :height (.-height r)}})))


                                  front-params (get-front-params)
                                  ]
                              (c/log "app front-params: " front-params)
                              (g/init-gl canvas)
                              (camera/init-camera-params! (:camera front-params))
                              (update-size)
                              (.addEventListener js/window "resize" (et/debounce update-size resize-timeout))
                              (.addEventListener js/document "keydown" (i/keys-handler (:keyboard-bindings front-params))))
                            )
     :reagent-render      (fn []
                            [:div {:style {:position "fixed"
                                           :top      0
                                           :bottom   0
                                           :left     0
                                           :right    0}}
                             [cn/console]

                             [:canvas {:id     "main-canvas"
                                       :width  "640"
                                       :height "480"
                                       }]])}))


(rd/render [app] (.getElementById js/document "app"))
