(ns scrap-world.app.graphics.core
  (:require [scrap-world.app.graphics.constants.clear-buffer-mask :as cb]))

(def gl-ctx (atom nil))


(defn init-gl [canvas]
  (let [gl (reset! gl-ctx (or
                            (.getContext canvas "webgl")
                            (.getContext canvas "experimental-webgl")))]
    (.clearColor gl 0.75 0.85 0.8 1.0))
  )

(defn change-viewport-size [{:keys [width height]}]
  (.viewport @gl-ctx 0 0 width height))

(defn draw-scene [params]
  (.clear @gl-ctx cb/color-buffer-bit))