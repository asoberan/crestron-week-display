(ns crestron-week-display.core
  (:require
   [crestron-week-display.config :as config]
   [crestron-week-display.server :as server])
  (:gen-class))

(defn -main
  [& args]
  (let [config (-> (first args)
                   config/load-config
                   config/validate-config!)]
    (server/start! config)))
