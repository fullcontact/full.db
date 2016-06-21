(defproject fullcontact/full.db "0.10.0-SNAPSHOT"
  :description "DB sugar (Korma + HarikiCP + core.async)."
  :url "https://github.com/fullcontact/full.db"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :deploy-repositories [["releases" {:url "https://clojars.org/repo/" :creds :gpg}]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [korma "0.4.2"]
                 [hikari-cp "1.5.0"]
                 [fullcontact/full.core "0.9.0"]
                 [fullcontact/full.metrics "0.9.0"]
                 [fullcontact/full.async "0.9.0"]
                 [org.liquibase/liquibase-core "3.3.5"]
                 [com.mattbertolini/liquibase-slf4j "1.2.1"
                  :exclusions [org.slf4j/slf4j-api
                               org.yaml/snakeyaml]]]
  :aot :all
  :plugins [[lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.7.0"]]}})
