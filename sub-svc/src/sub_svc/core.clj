(ns sub-svc.core
  (:require [com.stuartsierra.component :as c]
            [sub-svc.components :as components])
  (:gen-class))

(defn init []
  (alter-var-root #'sub-svc.system/current-system (constantly (components/system))))

(defn start []
  (alter-var-root #'sub-svc.system/current-system c/start))

(defn stop []
  (alter-var-root #'sub-svc.system/current-system #(when % (c/stop %) nil)))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (go))

(defn -main
  [& args]
  (go))
