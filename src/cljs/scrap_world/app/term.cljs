(ns scrap-world.app.term
  (:require
    [instaparse.core :as insta]))


(def gr "
tree       = <'['> <op-sp> split-type <sp> children <op-sp> <']'>
children   = (tree | node) (<sp> tree|node)*
node       = <'['> <op-sp> <':'> cmp-name <']'>
op-sp      = ' '*
sp         = ' '+
split-type = ':h'|':v'
cmp-name   = #'[a-zA-Z0-9_-]'
")

(def parser (insta/parser gr))

(def parsed (parser "[:h ]"))