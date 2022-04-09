(ns scrap-world.server.world.generation.primitive-world-generator
  (:require
    [clojure.spec.alpha :as s]
    [scrap-world.common-utils :as cu]
    [malli.core :as m]
    [malli.error :as me]
    ))


;(s/def ::cmd-name (s/and string? #(= "primitive-world-generator" %)))
;(s/def ::width (s/and int? pos? cu/is-pow-2?))
;(s/def ::length (s/and int? pos? cu/is-pow-2?))
;(s/def ::height (s/and int? pos?))
;(s/def ::seed string?)
;(s/def ::world-id string?)
;(s/def ::zero-level (s/and int? pos?))
;
;
;(s/def ::req (s/keys :req-un [::cmd-name ::width ::length ::height]
;                     :opt-un [::seed ::world-id ::zero-level]))



(def pow-of-2?
  (m/-simple-schema
    {:type            'pow-of-2?
     :pred            (fn [x] (and (pos? x) (zero? (bit-and x (dec x)))))
     :type-properties {:error/message "should be a power of 2"}}))

(def my-schema
  [:map
   [:cmd-name [:= "primitive"]]
   [:width [:and int? pos? pow-of-2?]]])


(m/form my-schema)

(m/explain my-schema {:cmd-name "primitive"
                      :width    2})

(-> my-schema
    (m/explain {:cmd-name "primitive"
                :width    3})
    (me/humanize))

(me/humanize (m/explain :string 1))