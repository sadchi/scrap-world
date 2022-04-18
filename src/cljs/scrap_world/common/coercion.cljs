(ns scrap-world.common.coercion)

(defn instaparse-fail->strs [{:keys [line column text reason]}]
  [(str "Parse error at line " line ", column " column ":") text
   (when (integer? column)
     (if (<= column 1) "^"
                       (apply str (concat (repeat (dec column) "\u00a0") [\^]))))
   (apply str reason)])
