(defproject fullcontact/full.db "0.10.4-SNAPSHOT"
  :description "DB sugar (Korma + hikariCP + core.async)."
  :url "https://github.com/fullcontact/full.db"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :deploy-repositories [["releases" {:url "https://clojars.org/repo/" :creds :gpg}]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [korma "0.4.2"]
                 [hikari-cp "1.5.0"]
                 [fullcontact/full.core "0.10.1" :exclusions [org.clojure/clojurescript]]
                 [fullcontact/full.metrics "0.11.4"]
                 [fullcontact/full.async "0.9.0"]
                 [org.liquibase/liquibase-core "3.3.5"]
                 [com.mattbertolini/liquibase-slf4j "1.2.1"
                  :exclusions [org.slf4j/slf4j-api org.yaml/snakeyaml]]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]
  :aot :all
  :plugins [[lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.7.0"]]}})
