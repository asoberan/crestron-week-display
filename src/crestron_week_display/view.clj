(ns crestron-week-display.view
  (:require [clojure.string :as str]
            [hiccup2.core :as h])
  (:import [java.time ZonedDateTime]))

(defn html-head
  [room-name]
  [:head
   [:meta {:http-equiv "refresh"
           :content "60"}]
   [:link {:type "text/css"
           :rel "stylesheet"
           :href "/css/style.css"}]
   [:title room-name]])

(defn calendar-title-bar
  [room-name schedule]
  (let [date-range (keys schedule)
        start (first date-range)
        end (last date-range)]
    [:div.calendar-title
     [:h1 room-name]
     [:div.range (str (str/capitalize (.getMonth start)) " " (.getDayOfMonth start)
                      " - "
                      (str/capitalize (.getMonth end)) " " (.getDayOfMonth end))]]))

(defn calendar-day-headers
  [schedule]
  [:div.calendar-headers
   [:div]
   (for [day (keys schedule)
         :let [day-of-week (.getDayOfWeek day)
               day-of-month (.getDayOfMonth day)]]
     [:div.day-head
      [:span.day-of-week day-of-week]
      [:span.day-of-month day-of-month]])])

(defn hour-labels
  []
  [:div.gutter
   (for [n (range 8 13)]
     [:div.hour-label (str n " AM")])
   (for [n (range 1 6)]
     [:div.hour-label (str n " PM")])])

(defn calendar-body
  []
  [:div.calendar-body
   (hour-labels)
   [:div "THIS WILL BE THE CALENDAR"]])

(defn calendar
  [{:keys [room-name status schedule]}]
  [:div.calendar
   (calendar-title-bar room-name schedule)
   (calendar-day-headers schedule)
   (calendar-body)])

(defn week-page
  [{:keys [room-name
           status
           schedule] :as room-data}]
  (h/html
      (h/raw "<!DOCTYPE html>")
      [:html
       (html-head room-name)
       [:body
        (calendar room-data)]]))
