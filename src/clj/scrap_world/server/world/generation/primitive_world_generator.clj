(ns scrap-world.server.world.generation.primitive-world-generator
  (:require
    [clojure.spec.alpha :as s]
    [scrap-world.common-utils :as cu]
    ))


(s/def ::cmd-name (s/and string? #(= "primitive-world-generator" %)))
(s/def ::width (s/and int? pos? cu/is-pow-2?))
(s/def ::length (s/and int? pos? cu/is-pow-2?))
(s/def ::height (s/and int? pos?))
(s/def ::seed string?)
(s/def ::world-id string?)
(s/def ::zero-level (s/and int? pos?))


(s/def ::req (s/keys :req-un [::cmd-name ::width ::length ::height]
                     :opt-un [::seed ::world-id ::zero-level]))
