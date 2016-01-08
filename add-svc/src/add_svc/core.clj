(ns add-svc.core
  (:require [com.stuartsierra.component :as c]
            [add-svc.components :as components])
  (:gen-class))

(defn init []
  (alter-var-root #'add-svc.system/current-system (constantly (components/system))))

(defn start []
  (alter-var-root #'add-svc.system/current-system c/start))

(defn stop []
  (alter-var-root #'add-svc.system/current-system #(when % (c/stop %) nil)))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (go))

(defn -main
  [& args]
  (go))
