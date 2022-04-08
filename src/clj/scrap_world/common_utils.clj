(ns scrap-world.common-utils)


(defn parse-int [s]
  (Integer. (re-find #"\d+" s)))

(defn is-pow-2? [^Long x]
  (and (pos? x) (zero? (bit-and x (dec x)))))
