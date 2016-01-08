(ns gateway.components
  (:require [com.stuartsierra.component :as component]
            [fusion-clj.pipeline :as p]
            [clojure.tools.logging :as log]
            [clj-logging-config.log4j :as log-config]
            [org.httpkit.server :as http]
            [gateway.config :as config]
            [gateway.routes :as r]))

(defrecord LoggingComponent [config]
  component/Lifecycle
  (start [this]
    (log-config/set-logger!
     "gateway"
     :name (-> config :logging :name)
     :level (-> config :logging :level)
     :out (-> config :logging :out))
    (log/logf :info "Environment is %s" (-> config :env))
    this)
  (stop [this]
    this))

(defrecord Pipeline [config logging]
  component/Lifecycle
  (start [this]
    (let [producer-config (-> config :kafka :producer)
          consumer-config (-> config :kafka :consumer)
          zk (:zk config)
          pipeline (p/pipeline producer-config :consumer-config consumer-config :zk zk)]
      (assoc this :pipeline pipeline)))
  (stop [this]
    (dissoc this :pipeline)))

(defrecord Router [config logging pipeline]
  component/Lifecycle
  (start [this]
    (assoc this :routes (r/app)))
  (stop [this]
    (dissoc this :routes)))

(defrecord Server [port config logging router]
  component/Lifecycle
  (start [this]
    (if (:stop! this)
      this
      (let [server (-> this
                       :router
                       :routes
                       (http/run-server {:port (or port 0)}))
            port (-> server meta :local-port)]
        (log/logf :info "Web server running on port %d" port)
        (assoc this :stop! server :port port))))
  (stop [this]
    (when-let [stop! (:stop! this)]
      (stop! :timeout 250))
    (dissoc this :stop! :router :port)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; High Level Application System
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn system
  [{:keys [port] :as options}]
  (component/system-map
   :config           (component/using (config/lookup) [])
   :logging          (component/using (map->LoggingComponent {}) [:config])
   :pipeline         (component/using (map->Pipeline {}) [:config :logging])
   :router           (component/using (map->Router {}) [:config :logging :pipeline])
   :server           (component/using (map->Server {:port (java.lang.Integer. port)}) [:config :logging :router])))
