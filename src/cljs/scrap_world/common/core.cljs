(ns scrap-world.common.core
  (:require
    [cljs-time.core :as t]
    [clojure.string :as s]
    [clojure.walk :as w]
    [garden.core :refer [css]]
    ))

(defn ^:private node [tag content ns]
  (let [elem (.createElement js/document tag)]
    (set! (.-innerHTML elem) content)
    (.setAttribute elem "ns" ns)
    elem))

(defn add-style! [css & {ns :ns}]
  (.appendChild (.-head js/document) (node "style" css ns)))

(defn is-css? [[_ x]]
  (get (meta @x) :css))

(defn mk-garden-desc [x]
  "pairs [sym-name sym-var] got from the ns-interns"
  (let [[sym-name sym-var] x
        class-keyword (keyword (str "." (name sym-name)))
        class-val @sym-var]
    (if (vector? class-val)
      (into [class-keyword] class-val)
      [class-keyword class-val])))

(defn mk-ns-classes [interns]
  (let [css-vars (filter is-css? (sort-by first interns))
        ;_ (log-o "css vars " css-vars)
        css-structs (map mk-garden-desc css-vars)
        ;_ (log-o "css vars " (str css-structs))
        ]
    css-structs))

(defn class-names [& cls]
  (s/join " " (flatten (map (fn [x]
                              (cond
                                (vector? x) (map name x)
                                (list? x) (map name x)
                                :else (name x))) cls))))

(defn cls [& args]
  (let [[classes extra] (split-with (complement keyword?) args)
        classes (apply class-names (filter some? classes))
        pre-map (partition 2 (into [:class classes] extra))
        full-map (reduce (fn [res [k v]]
                           (assoc res k v)) {} pre-map)]
    full-map))

(def css-w-prefixes
  (partial css {:vendors     ["webkit" "mos" "ms"]
                :auto-prefix #{:animation-name :animation-duration :animation-iteration-count :animation-timing-function :flex :appearance :flex-grow :flex-wrap :flex-shrink :flex-direction :align-content :align-self :justify-content :align-items :flex-basis}}))

(defn log [& msgs]
  (.log js/console (apply str msgs)))

(defn log-o [s o]
  (.log js/console s (clj->js o)))

(defn log-n-return [o]
  (.log js/console (w/postwalk (fn [x] (if (fn? x) "func" x)) o))
  o)


(defn add-css [interns ns-name & extra-classes]
  (let [start-t (t/now)
        css-classes (concat (mk-ns-classes interns) (apply concat extra-classes))]
    (add-style! (css-w-prefixes {:pretty-print? true} css-classes) :ns ns-name)
    (log (str ns-name " ... initialized, elapsed: " (- (t/now) start-t)))))

(defn wrap-f [x]
  (if (fn? x)
    x
    (fn [] x)))