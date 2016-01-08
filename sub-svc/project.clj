(defproject sub-svc "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [fusion-clj "0.1.3"]
                 [org.clojure/core.async "0.2.374"]
                 [com.stuartsierra/component "0.3.0"]
                 [clj-logging-config "1.9.12"]
                 [org.clojure/tools.logging "0.3.1"]]
  :main ^:skip-aot sub-svc.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
