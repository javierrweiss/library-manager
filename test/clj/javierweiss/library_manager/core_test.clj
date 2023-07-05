(ns javierweiss.library-manager.core-test
  (:require
   [javierweiss.library-manager.test-utils :as utils]
   [clojure.test :refer :all]
   [integrant.core :as ig]
   [clj-test-containers.core :as tc]
   [javierweiss.library-manager.db.db :as library-manager-db])
  (:import [org.testcontainers.containers CockroachContainer]))

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

(use-fixtures :once (utils/system-fixture))

(deftest pruebas_integracion_sql 
  (let [q (:db.sql/query-fn (utils/system-state))
        db "sql"
        user (library-manager-db/crear-usuario db q "Julia Marín" "juliamarin@gmail.com" "marin324" "Ksfws 434")
        id-u (:id (first user))
        author (library-manager-db/crear-autor db q "Rosario" "Millán")
        id-au (:id (first author))
        ref (library-manager-db/crear-referencia db q "Libro" "Los años de la locura" "2009" "Valdemoros" "Madrid" nil nil nil [id-au])
        id-ref (:referencia ref) 
        comentario (library-manager-db/crear-comentario db q "Este es un comentario muy juicioso" "120-156" "linux mercado opengl graalvm" id-ref id-u)
        id-com (:id (first comentario))
        cita (library-manager-db/crear-cita db q id-ref "Los azulejos son azules..." "120" id-u)
        id-cita (:id (first cita))
        coleccion (library-manager-db/crear-coleccion db q "Ciencias sociales" id-ref)
        id-col (:coleccion coleccion)
        biblioteca (library-manager-db/crear-biblioteca db q id-u "Biblioteca Mayor Simón Bolívar" id-col)
        id-biblioteca (:biblioteca biblioteca)]
    (testing "Crea usuario"
      (is (uuid? id-u)))
    (testing "Actualiza usuario" 
      (is (= 1 (library-manager-db/actualizar-usuario db q "cuenta" "juliamarin_90" id-u))))
    (testing "Crea autor"
      (is (uuid? id-au)))
    (testing "Actualiza autor"
      (is (= 1 (library-manager-db/actualizar-autor db q :apellidos "Linarez Alcántara" id-au))))
    (testing "Crea referencia"
      (is (uuid? id-ref)))
    (testing "Actualiza referencia"
      (is (= 1 (library-manager-db/actualizar-referencia db q :editorial "La Luz" id-ref))))
    (testing "Crea publicaciones"
      (is (= 1 (count (:publicaciones ref)))))
    (testing "Crea comentario"
      (is (uuid? id-com)))
    (testing "Actualiza comentario"
      (is (= 1 (library-manager-db/actualizar-comentario db q :comentario "Comente lo que comente, siempre hago un comentario" id-com))))
    (testing "Crea cita"
      (is (uuid? id-cita)))
    (testing "Actualiza cita"
      (is (= 1 (library-manager-db/actualizar-cita db q :paginas "19" id-cita))))
    (testing "Crear colección"
      (is (uuid? id-col)))
    (testing "Actualizar coleccion"
      (is (= 1 (library-manager-db/actualizar-coleccion db q :nombre_coll "Coleccion nueva" id-col))))
    (testing "Crear biblioteca"
      (is (uuid? id-biblioteca)))
    (testing "Actualizar biblioteca"
      (is (= 1 (library-manager-db/actualizar-biblioteca db q :nombre_biblioteca "Biblioteca de la Lora" id-biblioteca))))
    (testing "Eliminar biblioteca"
      (is (= 1 (library-manager-db/borrar-biblioteca db q id-biblioteca))))
    (testing "Eliminar coleccion"
      (is (= 1 (library-manager-db/borrar-coleccion db q id-col))))
    (testing "Elimina cita"
      (is (= 1 (library-manager-db/borrar-cita db q id-cita))))
    (testing "Elimina comentario"
      (is (= 1 (library-manager-db/borrar-comentario db q id-com))))
    (testing "Elimina referencia"
      (is (= 1 (library-manager-db/borrar-referencia db q id-ref))))
    (testing "Elimina usuario" 
      (is (= 1 (library-manager-db/borrar-usuario db q id-u))))
    (testing "Elimina autor"
      (is (= 1 (library-manager-db/borrar-autor db q id-au))))))
 
 
(comment
  (tap> (utils/system-state))
  (:db.sql/connection (utils/system-state))
  (:db/type (utils/system-state))
  (:db.sql/query-fn (utils/system-fixture))
  (def qn (:db.sql/query-fn (utils/system-state)))
  (tap> (utils/system-state))
  (def test-user (library-manager-db/crear-usuario "sql" qn "Pedro Montes" "pedromontes@gmail.com" "montes324" "Ksfws 434"))
  (tap> test-user)
  (def test-aut [(library-manager-db/crear-autor "sql" qn "Pedro" "Camoranesi")
                 (library-manager-db/crear-autor "sql" qn "Fulano" "DeTal")
                 (library-manager-db/crear-autor "sql" qn "Pilin" "Leon")])
  (tap> test-aut)
  (map :xt/id test-aut)
  (def test-ref (library-manager-db/crear-referencia
                 "sql"
                 qn
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
  (library-manager-db/crear-referencia
   "sql"
   qn
   "Libro"
   "Los perros"
   "2010"
   "Cachafaz"
   "Pilar"
   nil
   nil
   nil
   (vec (map :xt/id test-aut)))

  (library-manager-db/obtener-autores "sql" qn)

  (tap> test-ref)
  (tap> test-aut)
  (:id (first test-user))
  (->> (library-manager-db/obtener-usuarios "sql" qn)
       first
       :id
       (library-manager-db/borrar-usuario "sql" qn))

  (->> (library-manager-db/obtener-referencias "sql" qn)
       (map :id)
       (map #(library-manager-db/borrar-referencia "sql" qn %)))


  (as-> (map :id (library-manager-db/obtener-autores "sql" qn)) au
    (map #(library-manager-db/borrar-autor "sql" qn %) au))


  (tap> (run-test pruebas_integracion_sql))
  (run-tests)
  )
 