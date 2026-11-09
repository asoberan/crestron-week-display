(ns crestron-week-display.server
  (:require [crestron-week-display.schedule :as sched]
            [crestron-week-display.view :as view]
            [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]])
  (:import [java.time Instant ZoneId]))

(defn room-handler
  [config]
  (fn [request]
    (let [room-id (get-in request [:path-params :room-id])
          room (get (:rooms config) room-id)
          now (Instant/now)
          zone (ZoneId/of (:time-zone config))
          schedule (sched/week-schedule config now room-id)
          room-status (sched/current-status schedule zone now)]
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (view/week-page {:room-name (:name room)
                                   :status room-status
                                   :schedule schedule}))})))

(defn router
  [config]
  (ring/router [["/health" {:get (fn [_] {:status 200 :body "ok"})}]
                ["/room/:room-id" {:get (room-handler config)}]]))

(defn app
  [config]
  (-> (router config)
      (ring/ring-handler
       (ring/create-default-handler))
      (wrap-resource "public")
      (wrap-content-type)))

(defn start!
  [{:keys [port] :as config}]
  (jetty/run-jetty (app config) {:port port :join? false}))

(defn stop!
  [server]
  (.stop server))
