(ns crestron-week-display.schedule
  (:import [java.time LocalDateTime ZonedDateTime ZoneId]))

(set! *warn-on-reflection* true)

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

; Returns the schedule for the next work week.
; Vector of normalized events grouped by day of the week
; as LocalDate
(defn week-schedule
  [events zone]
  (let [get-day (fn [{:keys [^ZonedDateTime start]}]
                  (.toLocalDate start))]
    (->> events
         (map #(normalize-event % zone))
         (filter :visible?)
         (group-by get-day)
         (into (sorted-map)))))

; Given an event and now, returns whether the event
; is currently happening
(defn happening?
  [{:keys [^ZonedDateTime start
           ^ZonedDateTime end]} ^Instant now]
  (let [zoned-now (.atZone now (.getZone start))]
    (and
     (.isAfter zoned-now start)
     (.isBefore zoned-now end))))

; Returns the event happening next given a list of events
; and now
(defn get-next-event
  [events ^Instant now]
  (let [after? (fn [{:keys [^ZonedDateTime start]} ^Instant now]
                 (let [zoned-now (.atZone now (.getZone start))]
                   (.isAfter start zoned-now)))]
    (->> events
         (filter #(after? % now))
         (sort-by :start)
         first)))

; Given a list of events, determins the current status of the room.
; Either {:status :busy :until <time> :event <event>} or
; {:status :free :until <time>}
(defn current-status
  [events ^Instant now]
  (if-let [current-event (some #(when (happening? % now) %) events)]
    {:status :busy
     :until (:end current-event)
     :event current-event}
    {:status :free
     :until (:start (get-next-event all-events now))}))
