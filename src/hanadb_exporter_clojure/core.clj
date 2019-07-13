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

;; API SPECIFICATION 

;; 01) query:
;; (first (first metrics))

;; 02) meta-information for the query (enable, range) (Meta for the query) 
;; (second (first metrics)) 
;

;; 03 metrics: prometheus exporter defintions ( LIST)

;; e> (get-in (second (first metrics)) [:metrics])


;; create specific metric with labels
(defn register-gauge [{:keys [name description labels]}]
  (prometheus/gauge
    (keyword (str "hanadb/" name))
    {:description description
     :labels labels}))

;; register various metrics to registry
(defonce registry
  (-> (prometheus/collector-registry)
      (prometheus/register
         (prometheus/gauge     :hanadb/active-users-total)
       )))

;; Serve metrics
(defonce httpd
  (standalone/metrics-server registry {:port 8082}))

;; dump metrics via print
(defn dump-metric [] 
  (print (export/text-format registry))
)


