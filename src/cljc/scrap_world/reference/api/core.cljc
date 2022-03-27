(ns scrap-world.reference.api.core
  (:require
    [clojure.core.match :refer [match]]))


(defn is-alphabetical? [x]
  (boolean (re-matches #"[a-zA-Z]" (str x))))

(defn generate-uri [uri-template params & [log-f]]
  (let [preprocessed-uri-template (reduce
                                    (fn [{:keys [param-found res acc] :as old} c]
                                      #_(when log-f (log-f "generate-uri old: " old)
                                                    (log-f "generate-uri match: " param-found " " (= c \:) " " (is-alphabetical? c)))
                                      (match [param-found (= c \:) (is-alphabetical? c)]
                                             [false false _] {:param-found param-found
                                                              :res         (str res c)
                                                              :acc         acc}
                                             [false true _] {:param-found true
                                                             :res         res
                                                             :acc         ""}
                                             [true true _] {:param-found param-found
                                                            :res         res
                                                            :acc         (str acc c)}
                                             [true false true] {:param-found param-found
                                                                :res         res
                                                                :acc         (str acc c)}
                                             [true false false] {:param-found false
                                                                 :res         (str res (get params (keyword acc)) c)
                                                                 :acc         ""}
                                             :else old))
                                    {:param-found false
                                     :res         ""
                                     :acc         ""}
                                    uri-template)
        preprocessed-uri-template (if (:param-found preprocessed-uri-template)
                                    (update preprocessed-uri-template :res str (get params (keyword (:acc preprocessed-uri-template))))
                                    preprocessed-uri-template)]
    (when log-f
      (log-f "generate-uri template: " uri-template)
      (log-f "generate-uri params: " params)
      (log-f "generate-uri preprocessed-uri-template: " preprocessed-uri-template))
    (:res preprocessed-uri-template)))
