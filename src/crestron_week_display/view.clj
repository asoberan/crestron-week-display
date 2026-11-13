(ns crestron-week-display.view
  (:require [clojure.string :as str]
            [hiccup2.core :as h])
  (:import [java.time Duration ZonedDateTime]
           [java.time.format DateTimeFormatter]
           [java.time.temporal ChronoUnit]))

;; Convert a LocalDateTime to the number of minutes
;; since midnight. This should probably go into a
;; separate namespace.
(defn- minutes-from-midnight
  [time]
  (let [midnight (-> time .toLocalDate .atStartOfDay)]
    (.between (ChronoUnit/MINUTES) midnight time)))

;; Get the duration between a start and end. Both
;; are LocalDateTime. This should probably go into
;; a separate namespace
(defn- duration
  [start end]
  (.toMinutes (Duration/between start end)))

(defn html-head
  [room-name weekdays?]
  [:head
   [:meta {:http-equiv "refresh"
           :content "120"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   [:link {:type "text/css"
           :rel "stylesheet"
           :href "/css/style.css"}]
   [:style
    (str ".calendar-day-headers, .calendar-body { "
         "grid-template-columns: var(--gutter) repeat("
         (if weekdays? 7 5) ", minmax(0, 1fr)); }")]
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
  [schedule now]
  [:div.calendar-day-headers
   [:div]
   (for [day (keys schedule)
         :let [today? (= (.getDayOfWeek day) (.getDayOfWeek now))
               day-of-week (.getDayOfWeek day)
               day-of-month (.getDayOfMonth day)]]
     [:div.day-header
      (if today? {:class '[today]} {})
      [:span.day-of-week day-of-week]
      [:span.day-of-month day-of-month]])])

(defn hours-gutter
  []
  [:div.gutter
   (for [n (range 8 13)]
     [:div.hour-label (str n " AM")])
   (for [n (range 1 10)]
     [:div.hour-label (str n " PM")])])

(defn event-html
  [{:keys [title start end]}]
  (let [minutes-from-midnight (minutes-from-midnight start)
        duration (duration start end)
        format-time #(.format % (DateTimeFormatter/ofPattern "hh:mm a"))]
    [:article.event
     {:style (str "--start:" minutes-from-midnight "; "
                  "--duration:" duration)}
     [:div.title title]
     [:div.meta
      (str (format-time start) " - " (format-time end))]]))

(defn calendar-body
  [schedule now]
  [:div.calendar-body
   (hours-gutter)
   (for [[day events] schedule
         :let [today? (= (.getDayOfWeek day) (.getDayOfWeek now))]]
     [:section.day (if today? {:class '[today]} {})
      (for [event events]
        (event-html event))
      (when today? [:div.now {:style (str "--start:" (minutes-from-midnight now))}])])])

(defn calendar-footer
  [now]
  (let [formatter (DateTimeFormatter/ofPattern "hh:mm a")]
    [:div.calendar-footer
     (str "Last updated " (.format now formatter))]))

(defn calendar
  [{:keys [room-name status schedule]} now]
  [:div.calendar
   (calendar-title-bar room-name schedule)
   (calendar-day-headers schedule now)
   (calendar-body schedule now)
   (calendar-footer now)])

(defn week-page
  [{:keys [room-name
           weekdays?
           status
           schedule] :as room-data}
   now]
  (h/html
      (h/raw "<!DOCTYPE html>")
      [:html
       (html-head room-name weekdays?)
       [:body
        (calendar room-data now)]]))
