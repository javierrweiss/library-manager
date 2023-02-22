(ns javierweiss.library-manager.core-test
  (:require
   [javierweiss.library-manager.test-utils :as utils]
   [clojure.test :refer :all]
   [integrant.core :as ig]
   [clj-test-containers.core :as tc]
   [javierweiss.library-manager.db.db :as library-manager-db])
  (:import [org.testcontainers.containers CockroachContainer]))

(defmethod ig/init-key :db/type [_ {:keys [db-type]}] db-type)

(defmethod ig/init-key :db/testcontainers [_ _]
  (let [container (tc/init {:container (CockroachContainer. "cockroachdb/cockroach")
                            :exposed-ports [26257]})
        iniciado (tc/start! container)]
    {:jdbc-url (.getJdbcUrl (:container iniciado))
     :user (.getUsername (:container iniciado))
     :password (.getPassword (:container iniciado))
     :container container}))

(defmethod ig/halt-key! :db/testcontainers
  [_ {:keys [container]}]
  (tc/stop! container))

(utils/system-fixture)

(deftest pruebas_integracion_sql
  (let [q (:db.sql/query-fn (utils/system-state))
        user (library-manager-db/crear-usuario q "Julia Marín" "juliamarin@gmail.com" "marin324" "Ksfws 434")
        id-u (:id (first user))
        author (library-manager-db/crear-autor q "Rosario" "Millán")
        id-au (:id (first author))
        ref (library-manager-db/crear-referencia q "Libro" "Los años de la locura" "2009" "Valdemoros" "Madrid" nil nil nil [id-au])
        id-ref (:referencia ref)]
    (testing "Crea usuario"
      (is (uuid? id-u)))
    (testing "Actualiza usuario" 
      (is (= 1 (library-manager-db/actualizar-usuario q "cuenta" "juliamarin_90" id-u))))
    (testing "Crea autor"
      (is (uuid? id-au)))
    (testing "Actualiza autor"
      (is (= 1 (library-manager-db/actualizar-autor q :apellidos "Linarez Alcántara" id-au))))
    (testing "Crea referencia"
      (is (uuid? id-ref)))
    (testing "Actualiza referencia"
      (is (= 1 (library-manager-db/actualizar-referencia q :editorial "La Luz" id-ref))))
    (testing "Crea publicaciones"
      (is (= 1 (count (:publicaciones ref)))))
    (testing "Elimina referencia"
      (is (= 1 (library-manager-db/borrar-referencia q id-ref))))
    (testing "Elimina usuario" 
      (is (= 1 (library-manager-db/borrar-usuario q id-u))))
    (testing "Elimina autor"
      (is (= 1 (library-manager-db/borrar-autor q id-au))))))
 
 
(comment
  (utils/system-state)
  (:db.sql/connection (utils/system-state))
  (:db/type (utils/system-state))
  (:db.sql/query-fn (utils/system-fixture))
  (def qn (:db.sql/query-fn (utils/system-state)))
  (def test-user (library-manager-db/crear-usuario qn "Pedro Montes" "pedromontes@gmail.com" "montes324" "Ksfws 434"))
  (def test-aut [(library-manager-db/crear-autor qn "Pedro" "Camoranesi")
                 (library-manager-db/crear-autor qn "Fulano" "DeTal")
                 (library-manager-db/crear-autor qn "Pilin" "Leon")])
  (def test-ref (library-manager-db/crear-referencia qn
                                                     "Libro"
                                                     "Los perros"
                                                     "2010"
                                                     "Cachafaz"
                                                     "Pilar"
                                                     nil
                                                     nil
                                                     nil
                                                     [#uuid "2317eb29-30c0-4c7a-9b90-99ccdad4b0f4"
                                                      #uuid "97d8cfe9-f260-4657-b483-05ad52d6a5eb"
                                                      #uuid "9f0a9109-cb33-4765-93e7-87d0f92cd838"])) 
  (tap> test-ref) 
  (tap> test-aut)
  (:id (first test-user))
  (->> (library-manager-db/obtener-usuarios qn)
       first
       :id
       (library-manager-db/borrar-usuario qn))

  (->> (library-manager-db/obtener-referencias qn)
       (map :id)
       (map #(library-manager-db/borrar-referencia qn %)))

  (library-manager-db/obtener-publicaciones qn)

  (as-> (map :id (library-manager-db/obtener-autores qn)) au
    (map #(library-manager-db/borrar-autor qn %) au)) 

  (run-tests)
  )
