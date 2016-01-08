(ns gateway.routes
  (:require [clj-time.core :refer [before? after? now] :as t]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]
            [compojure.core :refer [routes GET PUT HEAD POST DELETE ANY context defroutes] :as compojure]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [gateway.controllers.equations :as equations]
            [gateway.system :refer [current-system]])
  (:import [java.util UUID]))

(defroutes api-routes
  (context "/api/v1" []
           (POST "/equations" []
                 (fn [r]
                   (let [pipeline (:pipeline current-system)]
                     (equations/process! (-> r :params :equation) pipeline))))))

(defroutes all-routes
  (GET "/" [] (io/resource "public/index.html"))
  (route/resources "/")
  (GET "/health-check" [] "<h1>I'm here</h1>")
  api-routes)

(defn wrap-stacktrace
  "ring.middleware.stacktrace only catches exception, not Throwable, so we replace it here."
  [handler]
  (fn [request]
    (try (handler request)
         (catch Throwable t
           (log/error t :request request)
           {:status 500
            :headers {"Content-Type" "text/plain; charset=UTF-8"}
            :body (with-out-str
                    (binding [*err* *out*]
                      (println "\n\nREQUEST:\n")
                      (pprint request)))}))))

(defn wrap-token
  "Add a unique token identifier to each request for easy debugging."
  [handler]
  (fn [request]
    (let [request-token (str (UUID/randomUUID))
          tokenized-request (assoc request :token request-token)]
      (log/debug (format "\n Start: %s \n Time: %s \n Request: \n %s"
                         request-token (t/now) request))
      (let [response (handler tokenized-request)]
        (log/debug (format "\n End: %s \n Time: %s \n Response: \n %s"
                           request-token (t/now) response))
        response))))


(defn app []
  (-> all-routes
      (wrap-restful-format :formats [:json-kw :edn])
      wrap-keyword-params
      wrap-params
      wrap-token
      wrap-stacktrace
      wrap-content-type))
