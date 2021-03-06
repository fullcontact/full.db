(ns full.db
  (:require [clojure.core.async :refer [chan >!! close!]]
            [clojure.core.async.impl.concurrent :refer [counted-thread-factory]]
            [korma.db :refer [default-connection create-db mysql with-db] :as kdb]
            [korma.core :refer [exec-raw]]
            [korma.config :refer [extract-options]]
            [hikari-cp.core :refer [make-datasource]]
            [full.core.config :refer [opt]]
            [full.metrics :refer [timeit gauge]]
            [full.core.sugar :refer :all]
            [clojure.string :as string])
  (:import (java.util.concurrent Executors)))


; See https://github.com/tomekw/hikari-cp#configuration-options
; for description of config options.
(def ^:private adapter (opt [:db :adapter] :default "mysql"))
(def ^:private server-name (opt [:db :server-name] :default nil))
(def ^:private database-name (opt [:db :database-name] :default nil))
(def ^:private url (opt [:db :url] :default nil))
(def ^:private port-number (opt [:db :port-number] :default nil))
(def ^:private username (opt [:db :username] :default nil))
(def ^:private password (opt [:db :password] :default nil))
(def ^:private auto-commit (opt [:db :auto-commit] :default true))
(def ^:private read-only (opt [:db :read-only] :default false))
(def ^:private connection-test-query (opt [:db :connection-test-query] :default nil))
(def ^:private connection-timeout (opt [:db :connection-timeout] :default 30000))
(def ^:private validation-timeout (opt [:db :validation-timeout] :default 5000))
(def ^:private idle-timeout (opt [:db :idle-timeout] :default 600000))
(def ^:private max-lifetime (opt [:db :max-lifetime] :default 1800000))
(def ^:private minimum-idle (opt [:db :minimum-idle] :default 10))
(def ^:private maximum-pool-size (opt [:db :maximum-pool-size] :default 10))
(def ^:private pool-name (opt [:db :pool-name] :default nil))
(def ^:private leak-detection-threshold (opt [:db :leak-detection-threshold] :default 0))
(def ^:private register-mbeans (opt [:db :register-mbeans] :default false))
(def ^:private data-source-properties (opt [:db :properties] :default {}))
(def ^:private driver-class-name (opt [:db :driver-class-name] :default nil))
(def ^:private jdbc-url (opt [:db :jdbc-url] :default nil))

(def db-specs {"firebird" kdb/firebird
               "postgres" kdb/postgres
               "oracle" kdb/oracle
               "mysql" kdb/mysql
               "vertica" kdb/vertica
               "mssql" kdb/mssql
               "msaccess" kdb/msaccess
               "sqlite3" kdb/sqlite3
               "h2" #(-> (kdb/h2 %)
                         ; see https://github.com/korma/Korma/issues/273#issuecomment-71812754
                         (assoc :delimiters ""
                                :naming {:keys string/lower-case
                                         :fields string/upper-case}))})

(def ^:private default-config
  (delay (?hash-map :adapter (when-not @driver-class-name @adapter)
                    :server-name @server-name
                    :database-name @database-name
                    :port-number @port-number
                    :url @url
                    :username @username
                    :password @password
                    :auto-commit @auto-commit
                    :read-only @read-only
                    :connection-test-query @connection-test-query
                    :connection-timeout @connection-timeout
                    :validation-timeout @validation-timeout
                    :idle-timeout @idle-timeout
                    :max-lifetime @max-lifetime
                    :minimum-idle @minimum-idle
                    :maximum-pool-size @maximum-pool-size
                    :pool-name @pool-name
                    :leak-detection-threshold @leak-detection-threshold
                    :register-mbeans @register-mbeans
                    :driver-class-name @driver-class-name
                    :jdbc-url @jdbc-url)))

(defn- create-connection
  [& {:keys [opts default-connection?]
      :or {default-connection? true}}]
  (let [spec (or (get db-specs @adapter) {})
        config (conj @default-config opts)
        ds (make-datasource config)]
    (doseq [[prop val] @data-source-properties]
      (.addDataSourceProperty ds (name prop) val))
    (let [conn {:pool {:datasource ds}
                :options (extract-options (spec {}))}]
      (cond-> conn default-connection? default-connection))))

(def db (delay (create-connection)))

(defn get-connection []
  (kdb/get-connection @db))

(defn get-custom-connection
  [& {:keys [default-connection? opts]
      :or {default-connection? false}}]
  (kdb/get-connection
    (create-connection :default-connection? default-connection?
                       :opts opts)))

(defmacro do
  [& body]
  ~@body)

(def thread-macro-executor
  (delay (Executors/newFixedThreadPool
           @maximum-pool-size
           (counted-thread-factory "db-thread-%d" true))))

(defn thread-call
  [f]
  (let [c (chan 1)]
    (try
      (let [binds (clojure.lang.Var/getThreadBindingFrame)]
        (.execute @thread-macro-executor
                  (fn []
                    (clojure.lang.Var/resetThreadBindingFrame binds)
                    (try
                      (let [ret (f)]
                        (when-not (nil? ret)
                          (>!! c ret)))
                      (finally
                        (close! c))))))
      (catch Exception e
        (>!! c e)
        (close! c)))
    c))

(def do-async-gauge (atom 0))

(defmacro do-async
  [& body]
  `(let [g# "full.db.do-async/gauge"]
     (gauge g# (swap! do-async-gauge inc))
     (thread-call
       (fn []
         (try
           ~@body
           (catch Throwable e# e#)
           (finally
             (gauge g# (swap! do-async-gauge dec))))))))

(defmacro do-async-timeit
  [k & body]
  `(do-async
     (timeit ~k
             ~@body)))

(defn ping> []
  (do-async
    (-> (exec-raw ["/* ping */ SELECT 1"] :results)
        first
        :1
        (= 1))))
