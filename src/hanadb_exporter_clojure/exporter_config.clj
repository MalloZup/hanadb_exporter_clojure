(ns hanadb-exporter-clojure.exporter-config
  (:require [iapetos.core :as prometheus]
       [cheshire.core :refer :all]))

(def config-file
  "hanadb-exporter config dir
  By default it's on PWD, but this can be overridden
  with the HANADB_EXPORTER_CONFIG_FILE env variable."
  (or (System/getenv "HANADB_EXPORTER_CONFIG_FILE") "config.json" ))

(defn read-config []
  (parse-stream (clojure.java.io/reader config-file) true))

(def config read-config)
