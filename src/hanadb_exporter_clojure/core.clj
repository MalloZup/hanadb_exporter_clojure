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
    (keyword (str name))
    {:description description
     :labels labels})))

(defn enabled-metrics [all-metrics]
  (remove #(false? (:enabled (second %))) all-metrics))

(defn check-if-in-hana-range [metrics]
  "check if the metric is inside to hanadb version range"
  (println "not yet"))

(defn register-all [metrics]
 "main function for register all type of metrics"
  ;; remove all disable metrics.
  ;; second entry is metadata
  ;; TODO: handle range of version hanadb (skip if not allowed)
    (doseq [entry (enabled-metrics metrics)]
    ;; sql-query, execute it (todo)
    (first entry) 
    ;; meta-data about the query  ;; (second entry) 
   
    ;; TODO filter metrics which are only gauge type
    (register-gauge hanadb-registry (first (get-in (second entry) [:metrics])))
    ))

;; register various metrics to registry
(defonce hanadb-registry
  (prometheus/collector-registry "hanadb")) ()

;; Serve metrics
(defonce httpd
  (standalone/metrics-server registry {:port 8082}))

;; dump metrics via print
(defn dump-metrics [] 
  (print (export/text-format hanadb-registry)))


