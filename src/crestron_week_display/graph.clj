(ns crestron-week-display.graph
  (:require [cheshire.core :as json]
            [hato.client :as hc])
  (:import [java.time DayOfWeek Instant ZonedDateTime ZoneId]
           [java.time.temporal TemporalAdjusters]))

(set! *warn-on-reflection* true)

(defonce ^:private token-cache (atom nil))

(defn- assoc-expires-at
  [{:keys [expires_in] :as token}]
  (assoc token :expires_at (.plusSeconds (Instant/now) expires_in)))

(defn- expires-within-5-minutes?
  [{:keys [^Instant expires_at] :as token}]
  (let [in-5-min (.plusSeconds (Instant/now) 300)]
    (.isBefore expires_at in-5-min)))

(defn fetch-token!
  [{:keys [tenant client-id client-secret]}]
  (let [resp (hc/post (str "https://login.microsoftonline.com/" tenant "/oauth2/v2.0/token")
                      {:form-params {:grant_type "client_credentials"
                                     :client_id client-id
                                     :client_secret client-secret
                                     :scope "https://graph.microsoft.com/.default"}
                       :as :json})]
    (when (= (:status resp) 200)
      (-> resp
          :body
          assoc-expires-at))))

; Read the cached token. If none exists, fetch a new one, place it in the cache, and return it.
; Otherwise, check the cached token's expiration. If it expires within 5 minutes, generate a new
; token, place it in the cache, and return it. Otherwise, return the cached token.
(defn access-token
  [config]
  (if-let [cached-token @token-cache]
    (if (expires-within-5-minutes? cached-token)
      (:access_token (reset! token-cache (fetch-token! config)))
      (:access_token cached-token))
    (:access_token (reset! token-cache (fetch-token! config)))))

; Return the work week for a corresponding now Instant.
; Assumes the work week starts on a Monday and lasts for
; 5 total days (e.g. Mon-Fri). May chaneg that in the future.
(defn work-week-bounds
  [{:keys [^String time-zone]} ^Instant now]
  (let [zone (ZoneId/of time-zone)
        zoned-now (.atZone now zone)
        local-date (.toLocalDate zoned-now)
        week-start (.with local-date (TemporalAdjusters/previousOrSame DayOfWeek/MONDAY))
        week-end (.plusDays week-start 5)]
    {:start (.atStartOfDay week-start zone)
     :end (.atStartOfDay week-end zone)}))

; Returns a vector of events. Each event has the following shape:
; {:@odata.etag "<ETAG>"
;  :id "<ID>"
;  :subject "Halloween Party"
;  :sensitivity "normal"
;  :isCancelled false
;  :showAs "busy"
;  :start {:dateTime "2026-11-02T18:00:00.0000000" :timeZone "UTC"}
;  :end {:dateTime "2026-11-09T18:00:00.0000000" :timeZone "UTC"}}
(defn week-events
  [config now room-id]
  (let [{:keys [^ZonedDateTime start ^ZonedDateTime end]} (work-week-bounds config now)
        room (get (:rooms config) room-id)
        resp (hc/get (str "https://graph.microsoft.com/v1.0/users/"
                          (:email room)
                          "/calendar/calendarView")
                     {:headers {"Authorization" (str "Bearer " (access-token config))
                                "Prefer" "outlook.timezone=\"UTC\""}
                      :query-params {"startDateTime" (str (.toInstant start)) ; "2026-11-02T00:00:00Z"
                                     "endDateTime" (str (.toInstant end)) ; "2026-11-09T00:00:00Z"
                                     "$select" "subject,start,end,isCancelled,showAs,sensitivity"
                                     "$top" "100"}
                      :as :json})]
    (when (= 200 (:status resp))
      (get-in resp [:body :value]))))
