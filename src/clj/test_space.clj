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
children   = (<op-sp> child)*
<child>    = tree|node
node       = <'['> <op-sp> <':'> cmp-name <op-sp> ( <sp> params)* <']'>
params     = <'{'> param ( <sp> param )* <'}'>
param      = keyword <sp> (str-val | simple-val)
op-sp      = ' '*
sp         = ' '+|newline+
split-type = ':h'|':v'
cmp-name   = #'[a-zA-Z0-9_-]+'
keyword    = <':'>#'[a-zA-Z0-9-_.]+'
simple-val = #'[a-zA-Z0-9-_.%]+'
str-val    = <'\"'> #'[^\"]*' <'\"'>
newline    = #'\n'
")


(def gr "
tree       = <'['> <op-sp> split-type <sp> children <op-sp> <']'>
children   = (<op-sp> child)*
<child>    = tree|node
node       = <'['> <op-sp> <':'> cmp-name <op-sp> size <']'>
size       = (#'[0-9]+' ('px'|'%')) | '*'
op-sp      = ' '*
sp         = ' '+|newline+
split-type = ':h'|':v'
cmp-name   = #'[a-zA-Z0-9_-]+'
newline    = #'\n'
")

(def parser (insta/parser gr))

(def parsed (parser "[:h [:console {:width 30% :size m}] [:log] [:v [:g {:s 2 :a 1}] [:f {:s 1 :a 2}] ]]"))





(def parsed (parser "[:h [:console 300px] [:log 30%] [:v [:g *] [:f 200px] ]]"))

(if (insta/failure? parsed)
  (doseq [s (instaparse-fail->strs parsed)]
    (println s))
  (println parsed))