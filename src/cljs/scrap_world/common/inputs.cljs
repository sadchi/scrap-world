(ns scrap-world.common.inputs
  (:require
    [garden.selectors :as s]
    [garden.units :refer [px]]
    [reagent.core :as r]
    [reagent.dom :as rd]
    [scrap-world.common.common-styles :as cs]
    [scrap-world.common.core :as c]
    [scrap-world.common.events-tools :as e]
    ))

(def params
  {::debounce-timeout 400
   ::padding          16
   ::height           40
   ::width            {::s 160
                       ::m 240
                       ::l 320}
   })

(defn v [& k]
  (get-in params k))


(def text-input ^:css {:height        (px (v ::height))
                       :width         (px (v ::width ::m))
                       :box-sizing    "border-box"
                       :outline       0
                       :border        0
                       :background    "rgba(255,255,255,0.5);"
                       :padding-left  (px (v ::padding))
                       :padding-right (px (v ::padding))})

(def text-input--transparent ^:css {:background   "rgba(0,0,0,0.3);"
                                    :border-width 0})

(def text-input--small ^:css {:width (px (v ::width ::s))})
(def text-input--big ^:css {:width (px (v ::width ::l))})
(def text-input--100 ^:css {:width "100%"})

(def check-box-css ^:css {:position "relative"
                          :bottom   "-2px"})

(def input-left-margin ^:css {:margin-left (px (v ::padding))})
(def input-right-margin ^:css {:margin-right (px (v ::padding))})


(def multiline-text-input ^:css {:height        "100%"
                                 :width         "100%"
                                 :resize        "none"
                                 :padding-left  (px 12)
                                 :padding-right (px 12)
                                 :box-sizing    "border-box"
                                 })


(defn input [size placeholder on-change-f & {:keys [id value autofocus autofocus-a external-val-a transparent]}]
  (let [val      (if-not (some? external-val-a)
                   (r/atom (str value))
                   external-val-a)
        f        (e/debounce
                   #(do
                      (c/log "debounce completed")
                      (when (some? on-change-f)
                        (on-change-f @val))) (v ::debounce-timeout))
        dom-elem (atom nil)]
    (r/create-class
      {:component-did-mount (fn [x]
                              (let [elem (rd/dom-node x)]
                                (c/log "$$$$ input elem: " elem)
                                (reset! dom-elem elem)
                                (when autofocus
                                  (.setTimeout js/window #(.focus elem) 50))))
       :reagent-render      (fn []
                              (when (some? @autofocus-a)
                                (if @autofocus-a
                                  (.setTimeout js/window #(.focus @dom-elem) 50)
                                  (.setTimeout js/window #(.blur @dom-elem) 50))
                                )
                              [:input (c/cls 'text-input
                                             (case size
                                               :l 'text-input--big
                                               :s 'text-input--small
                                               :100 'text-input--100
                                               nil)
                                             (when transparent 'text-input--transparent)
                                             :type "text"
                                             :value @val
                                             :placeholder placeholder
                                             :on-change (fn [x]
                                                          (let [new-val (-> x .-target .-value)]
                                                            (reset! val new-val)
                                                            (when (some? f) (f)))))])})))

(defn check-box [text on-change-f & [checked]]
  (fn []
    (c/log "check-box checked: " checked)
    [:label [:input (c/cls 'check-box-css
                           :type "checkbox"
                           :defaultChecked checked
                           :on-change #(if (.. % -target -checked) (on-change-f true)
                                                                   (on-change-f false)))] text]))

(defn check-box-bool [text on-change-f checked-f]
  (fn []

    [:label [:input (c/cls 'check-box-css
                           :type "checkbox"
                           :checked (if (some? checked-f)
                                      (checked-f)
                                      false)
                           :on-change #(if (.. % -target -checked) (on-change-f true)
                                                                   (on-change-f false)))] text]))



(defn radio [checked-f? on-change-f]
  [:input (c/cls :type "radio"
                 :checked (checked-f?)
                 :on-change on-change-f)])



(defn multiline-input [placeholder on-change-f & {:keys [id value autofocus external-val-a]}]
  (let [val      (if-not (some? external-val-a)
                   (r/atom (str value))
                   external-val-a)
        f        (e/debounce
                   #(do
                      (c/log "debounce completed")
                      (when (some? on-change-f)
                        (on-change-f @val))) (v ::debounce-timeout))
        dom-elem (atom nil)]
    (r/create-class
      {:component-did-mount (fn [x]
                              (let [elem (rd/dom-node x)]
                                (c/log "$$$$ input elem: " elem)
                                (reset! dom-elem elem)
                                (when autofocus
                                  (.setTimeout js/window #(.focus elem) 50))))
       :reagent-render      (fn []
                              [:textarea (c/cls 'multiline-text-input
                                                :value @val
                                                :placeholder placeholder
                                                :on-change (fn [x]
                                                             (let [new-val (-> x .-target .-value)]
                                                               (reset! val new-val)
                                                               (when (some? f) (f)))))])})))

(c/add-css (ns-interns 'scrap-world.common.inputs) (namespace ::x))
