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

(defn week-schedule
  [events zone]
  (->> events
       (map #(normalize-event % zone))
       (filter :visible?)))
