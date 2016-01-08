(ns mult-svc.lib
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer [<!!]])
  (:import [java.util UUID]))


(defn build-deps-map [tree args step]
  (if (= 0 (last args))
    tree
    (let [op-id (UUID/randomUUID)
          op-id-key (keyword (str op-id))
          deps-map {:topic "add"
                    :args (if (empty? tree)
                            [(first args) (first args)]
                            [(first args)])
                    :arg-in-fn conj
                    :deps (when-not (empty? tree)
                            (last (keys tree)))
                    :step step}]
      (recur (assoc tree op-id-key deps-map) [(first args) (dec (last args))] (inc step)))))

(defn deps-fn [msg]
  (let [args (-> msg :value :data)]
    (build-deps-map {} [(first args) (read-string (last args))] 1)))


(defn proc-fn [msg results]
  (let [ordered-results (into (sorted-map-by (fn [k1 k2]
                                               (compare [(-> results k2 :step) k2]
                                                        [(-> results k1 :step) k1]))) results)]
    (:result (first ordered-results))))

(defn read [chan]
  (log/info (<!! chan))
  (recur chan))
