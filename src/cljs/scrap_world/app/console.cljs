(ns scrap-world.app.console
  (:require
    [cljs-time.core :as t]
    [cljs-time.format :as tf]
    [garden.units :as g]
    [instaparse.core :as insta]
    [instaparse.failure :as fail]
    [reagent.core :as r]
    [reagent.dom :as rd]
    [scrap-world.common.common-styles :as cs]
    [scrap-world.common.core :as c]
    [scrap-world.common.inputs :as i]
    ))

(def params
  {::log-depth         100
   ::history-depth     30
   ::logs-depth        30
   ::timestamp-padding 170
   ::generic-padding   16
   ::background        "#9E9E9E"
   ::colors            {::bad     "#B00020"
                        ::good    "#004D40"
                        ::neutral "#212121"}})

(defn v [& k]
  (get-in params k))

(def g "
res             = per-cmd-help | generic-help | cmd | refresh
refresh         = <'refresh'>
generic-help    = <help-w>
per-cmd-help    = <help-w> <space> cmd-name
cmd             = cmd-name (<space> param)+
param           = keyword <space> (str-val | simple-val)
cmd-name        = #'[a-zA-Z0-9-_.]+'
keyword         = #':[a-zA-Z0-9-_.]+'
simple-val      = #'[a-zA-Z0-9-_.]+'
str-val         = <'\"'> #'[^\"]*' <'\"'>
<space>         = ' '+
<help-w>        = 'help'
")

(defonce parser (insta/parser g))

(def pre-transform {:num        (fn [& args]
                                  [:num (apply str args)])
                    :str        (fn [& args]
                                  [:str (apply str args)])
                    :var        (fn [& _] [:var])
                    :func-name  identity
                    :func-param identity
                    :math-sign  identity
                    :math-exp   identity})


(defn str->float [x]
  (js/parseFloat x))


(def post-transform {:num         (fn [x]
                                    (fn [_] (str->float x)))
                     :str         (fn [x]
                                    (fn [_] x))
                     :var         (fn [& _]
                                    (fn [v] v))

                     :math-op     (fn [x1 sign x2]
                                    (fn [v] ((case sign
                                               "+" +
                                               "-" -
                                               "*" *
                                               "/" /
                                               identity) (x1 v) (x2 v))))
                     :func-params (fn [& args]
                                    (fn [v]
                                      (map (fn [x] (x v)) args)))
                     :func        (fn [f-name params]
                                    (let [f (case f-name
                                              "abs" (fn [x] (max x (- x)))
                                              "max" max
                                              "min" min
                                              "eq" (fn [& [x y :as args]]
                                                     (c/log "@@@@@ eq " (apply str args))
                                                     (if (= x y)
                                                       1
                                                       0))
                                              "neq" (fn [& [x y :as args]]
                                                      (c/log "@@@@@ eq " (apply str args))
                                                      (if (= x y)
                                                        0
                                                        1))
                                              "contains" (fn [& [x y :as args]]
                                                           (c/log "@@@@@ eq " (apply str args))
                                                           #_(if (str/includes? x y)
                                                               1
                                                               0))
                                              "ncontains" (fn [& [x y :as args]]
                                                            (c/log "@@@@@ eq " (apply str args))
                                                            #_(if (str/includes? x y)
                                                                0
                                                                1))
                                              )]
                                      (fn [v]
                                        (apply f (params v)))))
                     :res         (fn [x]
                                    (fn [v] (x v)))})


(def console-pane ^:css [cs/flex-box
                         {:position       "absolute"
                          :top            0
                          :left           0
                          :right          0
                          :height         "75%"
                          :background     (::background params)
                          :border-width   "1px 0 1px 0"
                          :border-style   "solid"
                          :border-color   "rgba(0,0,0,0.2);"
                          :box-sizing     "border-box"
                          :flex-direction "column"
                          }])

(def console-pane--hidden ^:css {:top (g/px -40000)})

(def console-pane__logs ^:css [cs/mono-font
                               {:font-size (g/px 14)
                                :flex-grow 1
                                :position  "relative"}])

(def console-pane__logs__container ^:css {:position   "absolute"
                                          :left       0
                                          :right      0
                                          :top        0
                                          :bottom     0
                                          :overflow-y "auto"})

(def console-pane__logs__container__inner ^:css [cs/flex-box
                                                 {:flex-direction  "column"
                                                  :justify-content "flex-end"}])


(def console-pane__logs__item ^:css {:margin-top    (g/px 8)
                                     :margin-bottom (g/px 8)
                                     :position      "relative"
                                     :padding-left  (g/px (v ::timestamp-padding))
                                     :padding-right (g/px (v ::generic-padding))})

(def console-pane__logs__item--odd ^:css {:background "rgba(255, 255, 255, 0.2);"})

(def console-pane__logs__item--bad ^:css {:color (v ::colors ::bad)})
(def console-pane__logs__item--good ^:css {:color (v ::colors ::good)})
(def console-pane__logs__item--neutral ^:css {:color (v ::colors ::neutral)})

(def console-pane__logs__item__timestamp ^:css {:position "absolute"
                                                :left     (g/px (v ::generic-padding))})

(def console-pane__input ^:css {:flex-shrink   0
                                :margin-bottom "8px"})



(def console-opened? (r/atom false))
(def cmd-buffer (r/atom ""))
(def log-stack (r/atom (list)))
(def cmd-history (r/atom (list)))


(defn change-console-state []
  (c/log "change-console-state: " (swap! console-opened? not)))



(defn add-console-log [log]
  (swap! log-stack (fn [x] (take (v ::logs-depth) (conj x log)))))


(def custom-formatter (tf/formatter "yyyyMMdd HH:mm:ss"))

(defn run-cmd [cmd]
  (c/log "run-cmd cmd: " cmd)
  (let [cmd-parsed (parser cmd)]

    (if (insta/failure? cmd-parsed)
      (let [{:keys [line column text reason]} cmd-parsed]
        (add-console-log {::timestamp (tf/unparse custom-formatter (t/now))
                          ::color     :bad
                          ::content   [(str "Parse error at line " line ", column " column ":") text
                                       (when (integer? column)
                                         (if (<= column 1) "^"
                                                           (apply str (concat (repeat (dec column) "\u00a0") [\^]))))
                                       (apply str reason)]
                          }))
      (c/log "run-cmd parsed-cmd: " cmd-parsed))))

(defn console-logs []
  (let [node (atom nil)]
    (r/create-class
      {:component-did-mount  (fn [_]
                               (reset! node (js/document.getElementById "logs")))
       :component-did-update (fn [& _]
                               (.scrollTo @node 0 (.-scrollHeight @node)))
       :reagent-render       (fn []
                               [:div (c/cls 'console-pane__logs)
                                [:div#logs (c/cls 'console-pane__logs__container)
                                 [:div (c/cls 'console-pane__logs__container__inner)
                                  (doall (for [[idx item] (map-indexed vector (reverse @log-stack))
                                               :let [{::keys [timestamp color content]} item]]
                                           ^{:key idx} [:div (c/cls 'console-pane__logs__item
                                                                    (when (odd? idx) 'console-pane__logs__item--odd)
                                                                    (case color
                                                                      :bad 'console-pane__logs__item--bad
                                                                      :good 'console-pane__logs__item--good
                                                                      :else 'console-pane__logs__item--neutral))
                                                        [:div (c/cls 'console-pane__logs__item__timestamp) timestamp]
                                                        (for [[i x] (map-indexed vector content)]
                                                          ^{:key i} [:div x])]))]]])})))

(defn console []
  [:div (c/cls 'console-pane
               (when-not @console-opened? 'console-pane--hidden))
   [console-logs]
   [:div (c/cls 'console-pane__input
                :on-key-down (fn [e]
                               (condp = (.-which e)
                                 192 (.preventDefault e)
                                 27 (reset! cmd-buffer "")
                                 13 (.setTimeout js/window
                                                 #(let [cmd @cmd-buffer]
                                                    (swap! cmd-history (fn [x] (if (= cmd (first x))
                                                                                 x
                                                                                 (take (v ::history-depth) (conj x cmd)))))
                                                    (reset! cmd-buffer "")
                                                    (run-cmd cmd)) 100)
                                 nil)))
    [i/input :100 "" (fn []) :autofocus-a console-opened? :external-val-a cmd-buffer]]])


(c/add-css (ns-interns 'scrap-world.app.console)
           (namespace ::x))