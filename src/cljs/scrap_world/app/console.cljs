(ns scrap-world.app.console
  (:require
    [garden.selectors :as s]
    [garden.units :refer [px]]
    [reagent.core :as r]
    [scrap-world.common.core :as c]
    [scrap-world.common.common-styles :as cs]
    [scrap-world.common.inputs :as i]
    ))

(def params
  {::log-depth     100
   ::history-depth 30

   ::background    "rgba(255,255,255,0.4);"})

(defn v [& k]
  (get-in params k))





(def console-pane ^:css [cs/flex-box
                         {:position       "absolute"
                          :top            0
                          :left           0
                          :right          0
                          :height         "75%"
                          :background     (::background params)
                          :border-width   "1 0 1 0"
                          :border-style   "solid"
                          :border-color   "rgba(0,0,0,0.2);"
                          :box-sizing     "border-box"
                          :flex-direction "column"
                          }])

(def console-pane--hidden ^:css {:top (px -40000)})

(def console-pane__logs ^:css {:flex-grow 1
                               :position  "relative"})

(def console-pane__logs__content)

(def console-pane__input ^:css {:flex-shrink   0
                                :margin-bottom "8px"})

(def console-opened? (r/atom false))
(def cmd-buffer (r/atom ""))
(def log-stack (r/atom (list)))
(def cmd-history (r/atom (list)))


(defn change-console-state []
  (c/log "change-console-state: " (swap! console-opened? not)))

(defn run-cmd [cmd]
  )

(defn console []
  [:div (c/cls 'console-pane
               (when-not @console-opened? 'console-pane--hidden))
   [:div (c/cls 'console-pane__logs) "ha ha"]
   [:div (c/cls 'console-pane__input
                :on-key-down (fn [e]
                               (condp = (.-which e)
                                 192 (.preventDefault e)
                                 27 (reset! cmd-buffer "")
                                 13 (.setTimeout js/window
                                                 #(let [cmd @cmd-buffer]
                                                    (swap! cmd-history (fn [x] (if (= cmd (first x))
                                                                                 x
                                                                                 (->> (conj x cmd)
                                                                                      (take (v ::history-depth))))))
                                                    (run-cmd cmd)) 50)
                                 nil)))
    [i/input :100 "" (fn []) :autofocus-a console-opened? :external-val-a cmd-buffer]]])


(c/add-css (ns-interns 'scrap-world.app.console)
           (namespace ::x))