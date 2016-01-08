(defproject gateway "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.apache.commons/commons-daemon "1.0.9"]
                 [clj-logging-config "1.9.12"]
                 [clj-kafka "0.3.4"]
                 [clj-time "0.8.0" :exclusions [joda-time]]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.18"]
                 [fusion-clj "0.1.3"]
                 [ring-middleware-format "0.7.0"]
                 [ring/ring-core "1.4.0" :exclusions [joda-time]]
                 [com.stuartsierra/component "0.3.0"]
                 [javax.servlet/servlet-api "2.5"]]
  :main ^:skip-aot gateway.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
