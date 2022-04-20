(ns scrap-world.app.term
  (:require
    [instaparse.core :as insta]))


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

