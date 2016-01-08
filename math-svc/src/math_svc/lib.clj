(ns math-svc.lib
  (:require [clojure.core.async :refer [<!!]]
            [clojure.tools.logging :as log])
  (:import [java.util UUID]))

(def sym-topic-names
  {"*" "multiply"
   "/" "divide"
   "+" "add"
   "-" "subtract"})

(def operation-order ["*" "/" "+" "-"])

(defn eq->seq [equation]
  (map str equation))

(defn build-deps-tree [tree eq-seq step]
  (if (every? #(instance? java.util.UUID %) eq-seq)
    tree
    (let [op (first (drop-while #(= -1 (.indexOf eq-seq %)) operation-order))
          op-idx (.indexOf eq-seq op)
          leftv (nth eq-seq (dec op-idx))
          rightv (nth eq-seq (inc op-idx))
          deps (when-not (and (str? leftv) (str? rightv))
                 (reduce (fn [a v] (if (instance? java.util.UUID v)
                                    (conj a (keyword (str v)))
                                    a)) [] [leftv rightv]))
          arg-in-fn (when deps
                      (if (= 2 (count deps))
                        conj
                        (if (str? leftv)
                          conj
                          cons)))
          args (reduce (fn [a v]
                         (if-not (instance? java.util.UUID v)
                           (conj a v)
                           a)) [] [leftv rightv])
          op-id (UUID/randomUUID)
          op-id-key (keyword (str op-id))
          deps-map (-> {:topic (get sym-topic-names op)
                        :args args
                        :step step}
                       #(if deps
                          (assoc % :deps deps :arg-in-fn arg-in-fn)
                          %))
          eq-seq-beg (conj (first (split-at (dec op-idx) eq-seq)) op-id)
          eq-seq* (concat eq-seq-beg (last (split-at (+ 2 op-idx) eq-seq)))]
      (recur (assoc tree op-id-key deps-map) eq-seq* (inc step)))))

(defn deps-fn [msg]
  (build-deps-tree {} (eq->seq (-> msg :value :data)) 1))

(defn proc-fn [msg results]
  (let [ordered-results (into (sorted-map-by (fn [k1 k2]
                                               (compare [(-> results k2 :step) k2]
                                                        [(-> results k1 :step) k1]))) results)]
    (:result (first ordered-results))))

(defn read [chan]
  (log/info (<!! chan))
  (recur chan))
