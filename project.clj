(defproject hanadb_exporter_clojure "0.1.0-SNAPSHOT"
  :description "hanadb clojure prometheus exporter"
  :url "https://github.com/MalloZup/hanadb_exporter_clojure"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [seancorfield/next.jdbc "1.0.1"]
                 [iapetos "0.1.8"]
                 [cheshire "5.8.1"]
                 [local/ngdbc "2.4.56"]]

  :repositories {"local" "file:repo"}
  :repl-options {:init-ns hanadb-exporter-clojure.core})
