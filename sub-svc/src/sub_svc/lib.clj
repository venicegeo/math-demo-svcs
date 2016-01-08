(ns sub-svc.lib
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer [<!!]]))

(defn deps-fn [_]
  nil)

(defn proc-fn [msg & _]
  (let [args (map read-string (-> msg :value :data))]
    (apply - args)))

(defn read [chan]
  (log/info (<!! chan))
  (recur chan))
