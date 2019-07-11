# hanadb_exporter_clojure

Clojure hanadb exporter for Prometheus

## Usage

This project is experimental.

However it will follow the same API defined by https://github.com/SUSE/hanadb_exporter.

Especially the `metrics.json` file for hanadb queries and config.json for configuration.

## New jar 

0) Download the client

1) Create the local jar with:
``` mvn deploy:deploy-file -DgroupId=local -DartifactId=local -Dversion=2.4.56 -Dpackaging=jar -Dfile=ngdbc.jar -Durl=file:repo```

## License

Copyright © 2019 Dario Maiocchi, SUSE Linux 
