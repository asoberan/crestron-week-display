(ns crestron-week-display.schedule
  (:require [crestron-week-display.graph :as graph])
  (:import [java.time DayOfWeek Instant LocalDateTime ZonedDateTime ZoneId ZoneOffset]
           [java.time.temporal TemporalAdjusters]))

(set! *warn-on-reflection* true)

; Convert now into a ZonedDateTime
(defn zoned-now
  [^Instant now zone]
  (.atZone now (ZoneId/of zone)))

; Return the work week for a corresponding now Instant.
; Assumes the work week starts on a Monday and lasts for
; 5 total days (e.g. Mon-Fri). May chaneg that in the future.
(defn work-week-bounds
  [^ZonedDateTime now]
  (let [zone (.getZone now)
        local-date (.toLocalDate now)
        week-start (.with local-date (TemporalAdjusters/previousOrSame DayOfWeek/MONDAY))
        week-end (.plusDays week-start 4)]
    {:start (.atStartOfDay week-start zone)
     :end (.atStartOfDay week-end zone)}))

; Convert an event's dateTime to the given timezone
(defn- event-time->zoned
  [{:keys [^String dateTime ^String timeZone]} ^ZoneId zone]
  (let [parsed-date (LocalDateTime/parse dateTime)
        zoned-date (.atZone parsed-date (ZoneId/of timeZone))]
    (.withZoneSameInstant zoned-date zone)))

; Returns an event in the shape:
; {:title "<TITLE>" # Either the subject, or "Busy" when subject is empty of event is private
;  :visible? true # True if event is not cancelled or not set as "free"
;  :start "<Zoned date time>"
;  :end "<Zoned date time>"}
(defn- normalize-event
  [{:keys [subject
           sensitivity
           isCancelled
           showAs
           start
           end]} ^ZoneId zone]
  (let [private? (or (= sensitivity "private")
                     (= sensitivity "confidential"))
        title (if (or private? (nil? subject) (= "" subject))
                "Busy"
                subject) 
        visible? (not (or isCancelled (= showAs "free")))]
    {:title title
     :visible? visible?
     :start (event-time->zoned start zone)
     :end (event-time->zoned end zone)}))

(defn week-schedule
  [events zone]
  (->> events
       (map #(normalize-event % zone))
       (filter :visible?)))
