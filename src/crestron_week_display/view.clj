(ns crestron-week-display.view
  (:require [clojure.string :as str]
            [hiccup2.core :as h])
  (:import [java.time ZonedDateTime]
           [java.time.format DateTimeFormatter]))

(defn html-head
  [room-name]
  [:head
   [:meta {:http-equiv "refresh"
           :content "120"}]
   [:link {:type "text/css"
           :rel "stylesheet"
           :href "/css/style.css"}]
   [:title room-name]])

(defn calendar-title-bar
  [room-name schedule]
  (let [date-range (keys schedule)
        start (first date-range)
        end (last date-range)]
    [:div.calendar-title-bar
     [:h1#calendar-title room-name]
     [:div.range (str (str/capitalize (.getMonth start)) " " (.getDayOfMonth start)
                      " - "
                      (str/capitalize (.getMonth end)) " " (.getDayOfMonth end))]]))

(defn calendar-day-headers
  [schedule]
  [:div.calendar-day-headers
   [:div]
   (for [day (keys schedule)
         :let [day-of-week (.getDayOfWeek day)
               day-of-month (.getDayOfMonth day)]]
     [:div.day-header
      [:span.day-of-week day-of-week]
      [:span.day-of-month day-of-month]])])

(defn hours-gutter
  []
  [:div.gutter
   (for [n (range 8 13)]
     [:div.hour-label (str n " AM")])
   (for [n (range 1 6)]
     [:div.hour-label (str n " PM")])])

(defn calendar-body
  [schedule now]
  [:div.calendar-body
   (hours-gutter)
   (for [[day events] schedule
         :let [today? (= (.getDayOfWeek day) (.getDayOfWeek now))]]
     [:section.day (if today? {:class '[today]} {})
      (for [{:keys [title start end]} events]
        [:article.event
         [:div.title title]
         [:div.meta]])])])

(defn calendar-footer
  [now]
  (let [formatter (DateTimeFormatter/ofPattern "hh:mm a")]
    [:div.calendar-footer
     (str "Last updated " (.format now formatter))]))

(defn calendar
  [{:keys [room-name status schedule]} now]
  [:div.calendar
   (calendar-title-bar room-name schedule)
   (calendar-day-headers schedule)
   (calendar-body schedule now)
   (calendar-footer now)])

(defn week-page
  [{:keys [room-name
           status
           schedule] :as room-data}
   now]
  (h/html
      (h/raw "<!DOCTYPE html>")
      [:html
       (html-head room-name)
       [:body
        (calendar room-data now)]]))
