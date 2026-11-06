(ns crestron-week-display.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/and string? #(re-matches email-regex %)))
(s/def ::name string?)
(s/def ::room (s/keys :req-un [::email ::name]))
(s/def ::rooms (s/map-of string? ::room))

(s/def ::tenant string?)
(s/def ::client-id string?)
(s/def ::client-secret string?)
(s/def ::port int?)
(s/def ::time-zone string?)

(s/def ::config (s/keys :req-un [::tenant ::client-id ::client-secret ::rooms]
                        :opt-un [::port ::time-zone]))

(def default-config {:port 3000
                     :time-zone "America/New_York"})

(defn load-config
  [file-path]
  (let [config-file (io/file file-path)]
    (if (.exists config-file)
      (merge default-config (edn/read-string (slurp config-file)))
      (throw (ex-info "Configuration file not found" {:path file-path})))))

(defn validate-config!
  [config]
  (if (s/valid? ::config config)
    config
    (let [explain-data (s/explain-data ::config config)]
      (s/explain ::config config)
      (throw (ex-info "Invalid configuration file"
                      {:type ::invalid-config
                       :problems (:clojure.spec.alpha/problems explain-data)})))))
