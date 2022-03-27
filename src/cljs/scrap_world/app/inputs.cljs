(ns scrap-world.app.inputs
  (:require
    [scrap-world.common.core :as c]
    [scrap-world.app.console :as con]
    [scrap-world.app.events-queue :as eq]
    ))


(defn keys-handler [key-bindings]
  (c/log "keys-handler got bindings: " key-bindings)
  (let [key->event (reduce (fn [m [event {:keys [key]}]]
                             (assoc m key event)) {} key-bindings)]
    (fn [e]
      (let [key (.-which e)
            event (get key->event key)]
        ;(c/log "keys-handler event: " event)
        (when (some? event)
          (.stopPropagation e)
          (if-not @con/console-opened?
            (eq/dispatch! {:type event})
            (when (= event :console) (eq/dispatch! {:type event}))))))))
