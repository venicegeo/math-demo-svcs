(ns div-svc.config)

(def base-log-config
  (if-not (empty? (System/getProperty "catalina.base"))
    {:name "catalina"
     :level :info
     :out (org.apache.log4j.FileAppender.
           (org.apache.log4j.PatternLayout.
            "%d{HH:mm:ss} %-5p %22.22t %-22.22c{2} %m%n")
           (str (. System getProperty "catalina.base")
                "/logs/tail_catalina.log")
           true)}
    {:name "console"
     :level :info
     :out (org.apache.log4j.ConsoleAppender.
           (org.apache.log4j.PatternLayout.
            "%d{HH:mm:ss} %-5p %22.22t %-22.22c{2} %m%n"))}))

(defn- get-config-value
  [key & [default]]
  (or (System/getenv key)
      (System/getProperty key)
      default))

(defn app-config []
  {:dev         {:kafka {:consumer {"zookeeper.connect" "localhost:2181"
                                    "group.id" "dev.divsvc"}
                         :producer {"bootstrap.servers" "localhost:9092"}}
                 :zk "localhost:2181"
                 :logging base-log-config
                 :env :dev}
   :test        {:kafka {:consumer {"zookeeper.connect" "localhost:2181"
                                    "group.id" "test.divsvc"}
                         :producer {"bootstrap.servers" "localhost:9092"}}
                 :zk "localhost:2181"
                 :logging base-log-config
                 :env :test}
   :staging     {:kafka {:consumer {"zookeeper.connect" (get-config-value "ZK_HOST")
                                    "group.id" (get-config-value "GROUP_ID")}
                         :producer {"bootstrap.servers" (get-config-value "KAFKA_BROKERS")}}
                 :zk (get-config-value "ZK_HOST")
                 :logging base-log-config
                 :env :staging}
   :integration {:kafka {:consumer {"zookeeper.connect" (get-config-value "ZK_HOST")
                                    "group.id" (get-config-value "GROUP_ID")}
                         :producer {"bootstrap.servers" (get-config-value "KAFKA_BROKERS")}}
                 :zk (get-config-value "ZK_HOST")
                 :logging base-log-config
                 :env :integration}
   :production  {:kafka {:consumer {"zookeeper.connect" (get-config-value "ZK_HOST")
                                    "group.id" (get-config-value "GROUP_ID")}
                         :producer {"bootstrap.servers" (get-config-value "KAFKA_BROKERS")}}
                 :zk (get-config-value "ZK_HOST")
                 :logging base-log-config
                 :env :production}})

(defn lookup []
  (let [env (keyword (get-config-value "ENV" "dev"))]
    (env (app-config))))
