(ns test-space
  (:require
    [instaparse.core :as insta]))


(defn instaparse-fail->strs [{:keys [line column text reason]}]
  [(str "Parse error at line " line ", column " column ":") text
   (when (integer? column)
     (if (<= column 1) "^"
                       (apply str (concat (repeat (dec column) " ") [\^]))))
   (apply str reason)])

(def gr "
tree       = <'['> <op-sp> split-type <sp> children <op-sp> <']'>
children   = (tree | node) (<sp> tree|node)*
node       = <'['> <op-sp> <':'> cmp-name <']'>
op-sp      = ' '*
sp         = ' '+
split-type = ':h'|':v'
cmp-name   = #'[a-zA-Z0-9_-]+'
")

(def parser (insta/parser gr))

(def parsed (parser "[:h [:console]]"))

(if (insta/failure? parsed)
  (doseq [s (instaparse-fail->strs parsed)]
    (println s))
  (println parsed))