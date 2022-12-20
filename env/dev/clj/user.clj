(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
    [clojure.pprint]
    [clojure.spec.alpha :as s]
    [clojure.tools.namespace.repl :as repl]
    [criterium.core :as c]                                  ;; benchmarking
    [expound.alpha :as expound]
    [integrant.core :as ig]
    [integrant.repl :refer [clear go halt prep init reset reset-all]]
    [integrant.repl.state :as state]
    [kit.api :as kit]
    [lambdaisland.classpath.watch-deps :as watch-deps]      ;; hot loading for deps 
    [javierweiss.library-manager.core :refer [start-app]])) ;; Si falta esta dependencia, no arranca el repl
 
;; uncomment to enable hot loading for deps
(watch-deps/start! {:aliases [:dev :test]})

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn dev-prep!
  []
  (integrant.repl/set-prep! (fn []
                              (-> (javierweiss.library-manager.config/system-config {:profile :dev})
                                  (ig/prep)))))

(defn test-prep!
  []
  (integrant.repl/set-prep! (fn []
                              (-> (javierweiss.library-manager.config/system-config {:profile :test})
                                  (ig/prep)))))

;; Can change this to test-prep! if want to run tests as the test profile in your repl
;; You can run tests in the dev profile, too, but there are some differences between
;; the two profiles.
(dev-prep!)

(repl/set-refresh-dirs "src/clj")

(def refresh repl/refresh)

(defn reset-db []
  (migratus.core/reset (:db.sql/migrations state/system)))

(defn rollback []
  (migratus.core/rollback (:db.sql/migrations state/system)))

(defn migrate []
  (migratus.core/migrate (:db.sql/migrations state/system)))

(def query-fn (:db.sql/query-fn state/system))


(comment
  (go)
  (reset)
  (reset-all)
  (clear)
  ;;Crear las tablas en el orden correcto y sin que que solape el timestamp
  (doseq [tabla ["usuarios-table"
                 "autores-table"
                 "referencias-table"
                 "publicaciones-table"
                 "citas-table"
                 "comentarios-table"
                 "colecciones-table"
                 "bibliotecas-table"
                 "biblioteca-items-table"
                 "coleccion-items-table"]]
    (migratus.core/create (:db.sql/migrations state/system) tabla)
    (Thread/sleep 1000))

  ;;No necesitaba crearla, ya estaba
  (def consulta (:db.sql/query-fn state/system))

  (consulta :crear-usuario! {:usuarios/correo "leonardoblanco@gmail.com"
                             :usuarios/nombre "Leonardo Blanco"
                             :usuarios/cuenta "leoblanco"
                             :usuarios/clave "El leo!! 2004"})

  (consulta :obtener-por-id {:table "usuarios"
                             :id #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b"})

  (consulta :borrar-por-id! {:table "usuarios"
                             :id #uuid "1a9bc41a-3990-4d6e-9025-4c21869be868"})

  (consulta :obtener-todo {:table "usuarios"})

  (consulta :crear-autor! {:autores/nombres "Reinhart"
                           :autores/apellidos "Koselleck"})
  
  (consulta :crear-cita! {:citas/usuario #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b"})
  
  )
