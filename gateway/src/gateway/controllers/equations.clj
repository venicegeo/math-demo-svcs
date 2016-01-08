(ns gateway.controllers.equations
  (:require [gateway.views.equations :as e]))

(defn process! [equation pipeline]
  (let [pipeline* (:pipeline pipeline)]
    (-> (pipeline* "math" equation true)
        :value
        (e/shape-response 200))))
