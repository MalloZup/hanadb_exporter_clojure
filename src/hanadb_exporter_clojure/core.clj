(ns hanadb-exporter-clojure.core
  (:require [iapetos.core :as prometheus]
            [iapetos.export :as export]
            [next.jdbc :as jdbc]
            [hanadb-exporter-clojure.exporter-config :as exporter]
            [cheshire.core :refer :all]
            [iapetos.standalone :as standalone])
;; This is how we would import some java classes
;;  (:import [com.sap.db.jdbc Address])
)

;; default db data, overwritten by config.json (not this is immutable!)
(def hanadb-default 
  {:dbtype "sap" :dbname "SYSTEMDB" :classname "com.sap.db.jdbc.Driver"
   :host "10.162.32.88" :port "30013" 
   :user "SYSTEM" :password "Linux01Linux"})

(def hanadb-conf
;; destructure config.json and take the hana part of configuration
  (let [{:keys [host port user password]} (get-in exporter/config [:hana] )]
      ;; override default value with config.json
      (merge hanadb-default {:host host :port port :user user :password password})))

(def ds (jdbc/get-datasource hanadb-conf))

(defn query-hanadb-version []
  "returns a namespaced map"
  (jdbc/execute-one! ds ["select * from sys.m_database"]))

(defn hanadb-version []
  "return vresion as string"
  (:M_DATABASE_/VERSION (query-hanadb-version)))

(defn query-exec [query]
  "exec query and return namespaced map"
  (jdbc/execute! ds [query]))

;; METRIC (todo) (move to a namespace later on)
(defn read-metrics []
  (parse-stream (clojure.java.io/reader "metrics.json")true))

(def metrics (read-metrics))

;; create specific metric with labels
(defn register-gauge [registry {:keys [name description labels]}]
  (prometheus/register registry  (prometheus/gauge
    (keyword (str "sap/" name))
    {:description description
     :labels labels})))

(defn get-enabled-metrics [all-metrics]
  (remove #(false? (:enabled (second %))) all-metrics))

(defn check-if-in-hana-range [metrics]
  "check if the metric is inside to hanadb version range"
  (println "not yet"))


;; register various metrics to registry
(defonce hanadb-registry
  (prometheus/collector-registry "hanadb")) ()

(defn register-all [metrics]
 "main function for register all type of metrics"
  ;; remove all disable metrics.
  ;; second entry is metadata
  ;; TODO: handle range of version hanadb (skip if not allowed)
    (doseq [entry (get-enabled-metrics metrics)]
      
    ;; TODO filter metrics which are only gauge type
    ;; IMPORTANT/HACK/TODO we should iterate over the :metrics vector, instead of just taking the first
    (register-gauge hanadb-registry (first (get-in (second entry) [:metrics])))
    ))


(defn save-values-to-gauges [metrics]
  "execute sql query and store the value to gauge"
  ;; TODO filter metrics that are gauge type only
  (doseq [entry (get-enabled-metrics metrics)]
    ;; this is for removing the ":" from :select. the string is impure.
    (let [query-result (query-exec (second (clojure.string/split (str (first entry)) #":")))
          ;; user value is also a vector, because for each query we have multiples
          prometheus-info ((first (get-in (second entry) [:metrics])))]
      ;; iterate over prometheus info 
      (map #(println ((keyword "value-fixme"))) query-result)
    )))

;; Serve metrics
(defonce httpd
  (standalone/metrics-server hanadb-registry {:port 8082}))

;; dump metrics via print
(defn dump-metrics [] 
  (print (export/text-format hanadb-registry)))

;; TEST data: this is a query example. remove later
 (def res (query-exec "SELECT host, ROUND(SUM(memory_size_in_total)/1024/1024) column_tables_used_mb FROM sys.m_cs_tables GROUP BY host;"))
