(ns scrap-world.core
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.tools.cli :refer [parse-opts]]
    [clojure.tools.logging :as log]
    [scrap-world.server.core :as server]
    )
  (:import (java.io FileNotFoundException))
  (:gen-class))


(def cli-options [["-c" "--cfg CONFIG" "path to the configuration" :default "./cfg/config.clj"]
                  ["-h" "--help"]])

(defn read-cfg [path]
  (when-not (.exists (io/file path))
    (throw (FileNotFoundException. path)))
  (edn/read-string (slurp path)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (log/debug (apply str (repeat 120 "*")))
  (let [cli-opts (parse-opts args cli-options)
        config-path (get-in cli-opts [:options :cfg])
        cfg (read-cfg config-path)]
    (log/debug "Starting with cfg: " cfg)
    (server/start cfg)
    (println "Started!")))
