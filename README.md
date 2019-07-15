# hanadb_exporter_clojure

Clojure hanadb exporter for Prometheus

# Rationale:

This project use clojure and jdbc.next and prometheus expoert for SAP-hana db.

# Status:

the status is experimental and on wip

## Usage

However it will follow the same API defined by https://github.com/SUSE/hanadb_exporter.

The API is defined by 2 json files:

- `config.json` which configure the exporter setup  and the db

- `metrics.json` which configure the metrics and query the prometheus exporter will perform.

## New jar 

0) Download the client

1) Create the local jar with:
``` mvn deploy:deploy-file -DgroupId=local -DartifactId=local -Dversion=2.4.56 -Dpackaging=jar -Dfile=ngdbc.jar -Durl=file:repo```

## License

Copyright © 2019 Dario Maiocchi, SUSE Linux 
