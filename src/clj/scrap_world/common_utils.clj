(ns scrap-world.common-utils)


(defn parse-int [s]
  (Integer. (re-find #"\d+" s)))