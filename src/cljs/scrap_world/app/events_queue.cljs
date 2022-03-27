(ns scrap-world.app.events-queue
  (:require [clojure.core.async :as a]))

(def depth 100)
(def events-ch (a/chan depth))

(defn dispatch!
  [event]
  (a/put! events-ch event))


