(defproject fullcontact/full.db "1.0.5-SNAPSHOT"
  :description "DB sugar (Korma + hikariCP + core.async)."
  :url "https://github.com/fullcontact/full.db"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :deploy-repositories [["releases" {:url "https://clojars.org/repo/" :creds :gpg}]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [korma "0.4.3"]
                 [hikari-cp "1.8.0"]
                 [fullcontact/full.core "1.0.4" :exclusions [org.clojure/clojurescript]]
                 [fullcontact/full.metrics "0.11.4"]
                 [fullcontact/full.async "1.0.0"]
                 [org.liquibase/liquibase-core "3.5.3"]
                 [com.mattbertolini/liquibase-slf4j "2.0.0"
                  :exclusions [org.slf4j/slf4j-api org.yaml/snakeyaml]]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]
  :aot :all)
