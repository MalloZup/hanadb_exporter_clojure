(ns hanadb-exporter-clojure.core
  (:require [iapetos.core :as prometheus]
            [iapetos.export :as export]
            [next.jdbc :as jdbc]
            [hanadb-exporter-clojure.exporter-config :as exporter]
            [cheshire.core :refer :all]
            [iapetos.standalone :as standalone])
;;  (:import [com.sap.db.jdbc Address])
)

;; default db data, overwritten by config.json (not this is immutable!)
(def hanadb-default 
  {:dbtype "sap" :dbname "SYSTEMDB" :classname "com.sap.db.jdbc.Driver"
   :host "10.162.32.88" :port "30013" 
   :user "SYSTEM" :password "Linux01Linux"})


(def hanadb-conf
;; destructure config.json,
;; take the hana part of configuration
  (let [{:keys [host port user password]} (get-in (exporter/config) [:hana] )]
      ;; override default value with config.json
      (merge hanadb-default {:host host :port port :user user :password password})
   ;; (jdbc/get-datasource db)
  ))

(def ds (jdbc/get-datasource hanadb-conf))


(defn query-hanadb-version []
"returns a namespaced maps"
(jdbc/execute-one! ds ["select * from sys.m_database"]))

(defn hanadb-version []
  "return vresion as string"
  (:M_DATABASE_/VERSION (query-hanadb-version))
)



;; this metrics are not real ones..
;; create specific metric with labels
(def job-latency-histogram
  (prometheus/histogram
    :hanadb/job-latency-seconds
    {:description "job execution latency by job type"
     :labels [:job-type]
     :buckets [1.0 5.0 7.5 10.0 12.5 15.0]}))

;; register various metrics to registry
(defonce registry
  (-> (prometheus/collector-registry)
      (prometheus/register
       (prometheus/histogram :hanadb/duration-seconds)
       (prometheus/gauge     :hanadb/active-users-total)
       (prometheus/counter   :hanadb/runs-total)
       job-latency-histogram
     )))


;; serve metrics
(defonce httpd
  (standalone/metrics-server registry {:port 8080}))

