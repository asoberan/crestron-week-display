(ns crestron-week-display.schedule
  (:import [java.time DayOfWeek Instant ZonedDateTime ZoneId]
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
