(ns javierweiss.library-manager.db_integration_test
  (:require [javierweiss.library-manager.db.db :as library-manager-db]
            [clj-test-containers.core :as tc] 
            [clojure.test :refer :all]
            [integrant.core :as ig]
            [javierweiss.library-manager.test-utils :as utils])
  (:import [org.testcontainers.containers CockroachContainer])) 
 
(defmethod ig/prep-key :db.sql/connection [_ config]
  (let [container (tc/init {:container (CockroachContainer. "cockroachdb/cockroach")
                            :exposed-ports [26257]
                            ;; :resource-path "script-integration-test-sql" ;;Está haciendo la migración, así que no voy a necesitar esto, ni el de abajo.
                            ;; :container-path "/docker-entrypoint-initdb.d/" ;;Sí necesito resolver el tema del tipo BYTE. Necesito encontrar uno que se acomode a postgres y a cockroack o puedo usar un container de cockroach
                            })
        iniciado (tc/start! container)]
    (tap> container)
    (tap> iniciado)
    (merge config {:jdbc-url (.getJdbcUrl (:container iniciado))
                   :user (.getUsername (:container iniciado))
                   :password (.getPassword (:container iniciado))
                   :container container})))

(use-fixtures :once (:db.sql/query-fn (utils/system-fixture))) 
  
(deftest pruebas_integracion_sql
      (let [user (library-manager-db/crear-usuario "Julia Marín" "juliamarin@gmail.com" "marin324" "Ksfws 434")]
        (testing "Crea usuario"
          (is (uuid? user)))
        (testing "Actualiza usuario"
          (is (   (library-manager-db/actualizar-usuario "cuenta" "juliamarin_90" user)))))) 

(comment 
  (tc/stop!)
  (:db.sql/connection (utils/system-state))
  (:db/type (utils/system-state)) 
  (:db.sql/query-fn (utils/system-fixture))
  (:db.sql/query-fn (utils/system-state))
  (library-manager-db/db-type) 
  (library-manager-db/query-fn)
  (library-manager-db/obtener-autores)
  )