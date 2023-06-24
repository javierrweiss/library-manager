(ns javierweiss.library-manager.core
  (:gen-class)
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [javierweiss.library-manager.config :as config]
    [javierweiss.library-manager.env :refer [defaults]]
    [javierweiss.library-manager.web.handler]
    ;; Routes
    [javierweiss.library-manager.web.routes.api]
    [javierweiss.library-manager.web.routes.ui]
    ;; Edges 
    [kit.edge.cache.redis]
    [kit.edge.db.postgres]
    [kit.edge.db.sql.conman]
    [kit.edge.db.sql.migratus]
    [kit.edge.db.xtdb]
    [kit.edge.http.hato]
    [kit.edge.scheduling.quartz]
    [kit.edge.server.undertow]
    [kit.edge.templating.selmer]
    [kit.edge.utils.metrics]
    [kit.edge.utils.repl]))


;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException
      [_ thread ex]
      (log/error {:what :uncaught-exception
                  :exception ex
                  :where (str "Uncaught exception on" (.getName thread))}))))


(defonce system (atom nil))


(defn stop-app
  []
  ((or (:stop defaults) (fn [])))
  (some-> (deref system) (ig/halt!))
  (shutdown-agents))


(defn start-app
  [& [params]]
  ((or (:start params) (:start defaults) (fn [])))
  (->> (config/system-config (or (:opts params) (:opts defaults) {}))
       (ig/prep)
       (ig/init)
       (reset! system))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))


(defn -main
  [& _]
  (start-app))
