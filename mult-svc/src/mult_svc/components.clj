(ns mult-svc.components
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [chan thread]]
            [clj-logging-config.log4j :as log-config]
            [mult-svc.config :as config]
            [mult-svc.lib :as l]
            [fusion-clj.reactor :as r]))

(defrecord LoggingComponent [config]
  component/Lifecycle
  (start [this]
    (log-config/set-logger!
     "mult-svc"
     :name (-> config :logging :name)
     :level (-> config :logging :level)
     :out (-> config :logging :out))
    (log/logf :info "Environment is %s" (-> config :env))
    this)
  (stop [this]
    this))

(defrecord Reactor [config logging]
  component/Lifecycle
  (start [this]
    (log/info "Building Reactor Component...")
    (let [consumer-config (-> config :kafka :consumer)
          producer-config (-> config :kafka :producer)
          zk (:zk config)
          out (chan)
          elements (r/elements consumer-config "multiply" producer-config zk out)
          reactor (r/reactor l/deps-fn l/proc-fn)]
      (do (reactor elements)
          (thread (l/read out))
          (assoc this :elements elements :reactor reactor))))
  (stop [this]
    (do (log/info "Shutting down Reactor...")
        (shutdown-reactor (:elements this))
        (dissoc this :elements :reactor))))

(defn system []
  (component/system-map
   :config  (component/using (config/lookup) [])
   :logging (component/using (map->LoggingComponent {}) [:config])
   :reactor (component/using (map->Reactor {}) [:config :logging])))
