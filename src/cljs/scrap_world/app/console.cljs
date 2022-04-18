(ns scrap-world.app.console
  (:require
    [cljs-time.core :as t]
    [cljs-time.format :as tf]
    [garden.units :as g]
    [instaparse.core :as insta]
    [reagent.core :as r]
    [scrap-world.common.common-styles :as cs]
    [scrap-world.common.core :as c]
    [scrap-world.common.inputs :as i]
    ))

(def params
  {::log-depth         100
   ::history-depth     30
   ::logs-depth        30
   ::timestamp-padding 190
   ::generic-padding   16
   ::background        "#9E9E9E"
   ::colors            {::bad     "#B00020"
                        ::good    "#004D40"
                        ::neutral "#212121"}})

(defn v [& k]
  (get-in params k))

(def gr "
res             = per-cmd-help | generic-help | cmd | refresh
refresh         = <'refresh'>
generic-help    = <help-w>
per-cmd-help    = <help-w> <space> cmd-name
cmd             = cmd-name (<space> param)+
param           = keyword <space> (str-val | simple-val)
cmd-name        = #'[a-zA-Z0-9-_.]+'
keyword         = <':'>#'[a-zA-Z0-9-_.]+'
simple-val      = #'[a-zA-Z0-9-_.]+'
str-val         = <'\"'> #'[^\"]*' <'\"'>
<space>         = ' '+
<help-w>        = 'help'
")

(defonce parser (insta/parser gr))




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
                          :box-shadow     "rgba(0, 0, 0, 0.1) 0px 20px 25px -5px, rgba(0, 0, 0, 0.04) 0px 10px 10px -5px;"
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


(def console-pane__logs__item ^:css {
                                     ;:margin-top    (g/px 8)
                                     ;:margin-bottom (g/px 8)
                                     :position      "relative"
                                     :padding-left  (g/px (v ::timestamp-padding))
                                     :padding-right (g/px (v ::generic-padding))})

(def console-pane__logs__item--odd ^:css {:background "rgba(255, 255, 255, 0.2);"})

(def console-pane__logs__item--bad ^:css {:color (v ::colors ::bad)})
(def console-pane__logs__item--good ^:css {:color (v ::colors ::good)})
(def console-pane__logs__item--neutral ^:css {:color (v ::colors ::neutral)})

(def console-pane__logs__item__timestamp ^:css {:position "absolute"
                                                :left     (g/px (v ::generic-padding))})

(def console-pane__input ^:css {:flex-shrink 0
                                ;:margin-bottom "8px"
                                })



(def console-opened? (r/atom false))
(def cmd-buffer (r/atom ""))
(def log-stack (r/atom (list)))
(def cmd-history (atom (list)))
(def cmd-history-pointer (atom 0))

(defn add-console-log [log]
  (swap! log-stack (fn [x] (take (v ::logs-depth) (conj x log)))))

(def custom-formatter (tf/formatter "yyyyMMdd HH:mm:ss"))

(def pre-transform {:cmd-name   identity
                    :param      (fn [& args]
                                  args)
                    :keyword    identity
                    :str-val    identity

                    :simple-val (fn [x]
                                  (if (js/isNaN x)
                                    x (js/parseFloat x)))
                    :num        (fn [& args]
                                  [:num (apply str args)])
                    :str        (fn [& args]
                                  [:str (apply str args)])
                    })




(def post-transform {:res          (fn [x] (fn [] (x)))

                     :generic-help (fn [& _]
                                     (fn []
                                       (add-console-log {::timestamp (tf/unparse custom-formatter (t/time-now))
                                                         ::color     :neutral
                                                         ::content   ["Try to use: 'help <command-name>'  to get per command info"
                                                                      "or 'refresh' to get commands list from the back end"]})))

                     :per-cmd-help (fn [x]
                                     (fn []
                                       (add-console-log {::timestamp (tf/unparse custom-formatter (t/time-now))
                                                         ::color     :neutral
                                                         ::content   [(str "Requested help for the <" x "> command")
                                                                      ]})))
                     :refresh      (fn [& _]
                                     (fn []
                                       (add-console-log {::timestamp (tf/unparse custom-formatter (t/time-now))
                                                         ::color     :neutral
                                                         ::content   [(str "Trying to get commands list from the back end")]})))
                     :cmd          (fn [cmd-name & params]
                                     (fn []
                                       (c/log "CMD cmd-name: " cmd-name)
                                       (c/log "CMD params: " params)
                                       (let [params-map (into {} params)
                                             req        {:command cmd-name
                                                         :params  params-map}]
                                         (add-console-log {::timestamp (tf/unparse custom-formatter (t/time-now))
                                                           ::color     :good
                                                           ::content   [(str "Executing command : " req)]}))))})

(defn change-console-state []
  (c/log "change-console-state: " (swap! console-opened? not)))








(defn run-cmd [cmd]
  (c/log "run-cmd cmd: " cmd)
  (let [cmd-parsed (parser cmd)]

    (if (insta/failure? cmd-parsed)
      (let [{:keys [line column text reason]} cmd-parsed]
        (add-console-log {::timestamp (tf/unparse custom-formatter (t/time-now))
                          ::color     :bad
                          ::content   [(str "Parse error at line " line ", column " column ":") text
                                       (when (integer? column)
                                         (if (<= column 1) "^"
                                                           (apply str (concat (repeat (dec column) "\u00a0") [\^]))))
                                       (apply str reason)]
                          }))
      (let [pre-transformed (insta/transform pre-transform cmd-parsed)
            res             (insta/transform post-transform pre-transformed)]

        (c/log "run-cmd parsed-cmd: " cmd-parsed)
        (c/log "run-cmd pre-transformed: " pre-transformed)
        (res)))))

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
                                                                      'console-pane__logs__item--neutral))
                                                        [:div (c/cls 'console-pane__logs__item__timestamp) timestamp]
                                                        (for [[i x] (map-indexed vector content)]
                                                          ^{:key i} [:div x])]))]]])})))


(defn get-cmd-from-history [f]
  (let [history-size (count @cmd-history)
        old-cmd      (nth @cmd-history @cmd-history-pointer)]
    (c/log "get-cmd-from-history new-pos:"
           (swap! cmd-history-pointer (fn [x]
                                        (rem (f (+ x history-size)) history-size))))
    (reset! cmd-buffer old-cmd)))

(defn console []
  [:div (c/cls 'console-pane
               (when-not @console-opened? 'console-pane--hidden))
   [console-logs]
   [:div (c/cls 'console-pane__input
                :on-key-down (fn [e]
                               (condp = (.-which e)
                                 192 (.preventDefault e)
                                 27 (reset! cmd-buffer "")
                                 38 (get-cmd-from-history inc)
                                 40 (get-cmd-from-history dec)
                                 13 (.setTimeout js/window
                                                 #(let [cmd @cmd-buffer]
                                                    (swap! cmd-history (fn [x] (if (= cmd (first x))
                                                                                 x
                                                                                 (take (v ::history-depth) (conj x cmd)))))
                                                    (reset! cmd-history-pointer 0)
                                                    (reset! cmd-buffer "")
                                                    (run-cmd cmd)) 100)
                                 nil)))
    [i/input :100 "" (fn []) :autofocus-a console-opened? :external-val-a cmd-buffer]]])


(c/add-css (ns-interns 'scrap-world.app.console)
           (namespace ::x))