(ns crestron-week-display.schedule
  (:require [crestron-week-display.graph :as graph])
  (:import [java.time DayOfWeek Instant LocalDateTime ZonedDateTime ZoneId]
           [java.time.temporal TemporalAdjusters]))

(set! *warn-on-reflection* true)

;; Convert an event's dateTime to the given timezone
(defn- event-time->zoned
  [{:keys [^String dateTime ^String timeZone]} ^ZoneId zone]
  (let [parsed-date (LocalDateTime/parse dateTime)
        zoned-date (.atZone parsed-date (ZoneId/of timeZone))]
    (.withZoneSameInstant zoned-date zone)))

;; Returns an event in the shape:
;; {:title "<TITLE>" # Either the subject, or "Busy" when subject is empty of event is private
;;  :visible? true # True if event is not cancelled or not set as "free"
;;  :start "<Zoned date time>"
;;  :end "<Zoned date time>"}
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

;; Return the work week for a corresponding now Instant.
;; Assumes the work week starts on a Monday and lasts for
;; 5 total days (e.g. Mon-Fri). May change that in the future.
(defn work-week-bounds
  [^ZoneId zone ^Instant now]
  (let [zoned-now (.atZone now zone)
        local-date (.toLocalDate zoned-now)
        week-start (.with local-date (TemporalAdjusters/previousOrSame DayOfWeek/MONDAY))
        week-end (.plusDays week-start 5)
        start (.atStartOfDay week-start zone)
        end (.atStartOfDay week-end zone)]
    {:start start
     :end end
     ;; Return the range of days between start and end in a collection
     ;; of LocalDate items
     :range (-> start
                .toLocalDate
                (.datesUntil (.toLocalDate end))
                (.collect (java.util.stream.Collectors/toList)))}))

;; Organize a collection of normalized-events into a map
;; with "day in range" as a key and "collection of events"
;; as a value. Days with no events are empty collections.
(defn- events-by-day
  [days-range events]
  (let [grouped (group-by #(.toLocalDate (:start %)) events)]
    (into (sorted-map)
          (map (fn [day] [day (get grouped day [])]))
          days-range)))

;; Returns the schedule for the next work week.
;; Vector of normalized events grouped by day of the week
;; as LocalDate
(defn week-schedule
  [config now room-id]
  (let [zone (ZoneId/of (:time-zone config))
        week-bounds (work-week-bounds zone now)
        events (graph/week-events config week-bounds now room-id)]
    (->> events
         (map #(normalize-event % zone))
         (filter :visible?)
         (events-by-day (:range week-bounds)))))

;; Given an event and now, returns whether the event
;; is currently happening
(defn happening?
  [{:keys [^ZonedDateTime start
           ^ZonedDateTime end]} ^Instant now]
  (let [zoned-now (.atZone now (.getZone start))]
    (and
     (.isAfter zoned-now start)
     (.isBefore zoned-now end))))

;; Returns the event happening next given a list of events
;; and now
(defn get-next-event
  [events ^Instant now]
  (let [after? (fn [{:keys [^ZonedDateTime start]} ^Instant now]
                 (let [zoned-now (.atZone now (.getZone start))]
                   (.isAfter start zoned-now)))]
    (->> events
         (filter #(after? % now))
         (sort-by :start)
         first)))

;; Given a list of events, determins the current status of the room.
;; Either {:status :busy :until <time> :event <event>} or
;; {:status :free :until <time>}
(defn current-status
  [schedule ^ZoneId zone ^Instant now]
  (let [today (.toLocalDate (.atZone now zone))]
    (if-let [current-event (some #(when (happening? % now) %)
                                 (get schedule today))]
      {:status :busy
       :until (:end current-event)
       :event current-event}
      {:status :free
       :until nil
       ;;(:start (get-next-event events now))
       })))
