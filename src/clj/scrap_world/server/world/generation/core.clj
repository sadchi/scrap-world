(ns scrap-world.server.world.generation.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::cmd-name )

(s/def primitive-world-generate (s/keys :req-un [::width ::length ::height]
                                        :opt-un [::seed ::zero-level]))



