(ns crestron-week-display.server
  (:require [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty]))

(defn app
  []
  (ring/ring-handler
   (ring/router [["/health" {:get (fn [_] {:status 200 :body "ok"})}]])
   (ring/create-default-handler)))

(defn start!
  [{:keys [port]}]
  (jetty/run-jetty (app) {:port port :join? false}))

(defn stop!
  [server]
  (.stop server))
