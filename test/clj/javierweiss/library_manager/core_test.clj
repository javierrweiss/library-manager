(ns javierweiss.library-manager.core-test
  (:require
   [javierweiss.library-manager.test-utils :as utils]
   [clojure.test :refer :all]
   [integrant.core :as ig]
   [clj-test-containers.core :as tc]
   [javierweiss.library-manager.db.db :as library-manager-db]
   [xtdb.api :as xtdb])
  (:import [org.testcontainers.containers CockroachContainer]))

(defmethod ig/init-key :db/testcontainers [_ m]
  (let [container (tc/init {:container (CockroachContainer. "cockroachdb/cockroach")
                            :exposed-ports [26257]})
        iniciado (tc/start! container)]
    (merge m {:jdbc-url (.getJdbcUrl (:container iniciado))
              :user (.getUsername (:container iniciado))
              :password (.getPassword (:container iniciado))
              :container container})))

(defmethod ig/halt-key! :db/testcontainers
  [_ {:keys [container]}]
  (tc/stop! container))

(use-fixtures :once (utils/system-fixture))

(deftest pruebas_integracion_sql 
  (let [state (let [state-map (utils/system-state)
                    sql (:db.sql/query-fn state-map)
                    final-map (second (:reitit.routes/api state-map))]
                (assoc final-map :query-fn sql :db-type :sql)) 
        user (library-manager-db/crear-usuario state "Julia Marín" "juliamarin@gmail.com" "marin324" "Ksfws 434") 
        id-u (:id (first user))
        author (library-manager-db/crear-autor state "Rosario" "Millán")
        id-au (:id (first author))
        ref (library-manager-db/crear-referencia state "Libro" "Los años de la locura" "2009" "Valdemoros" "Madrid" nil nil nil [id-au])
        id-ref (:referencia ref) 
        comentario (library-manager-db/crear-comentario state "Este es un comentario muy juicioso" "120-156" "linux mercado opengl graalvm" id-ref id-u)
        id-com (:id (first comentario))
        cita (library-manager-db/crear-cita state id-ref "Los azulejos son azules..." "120" id-u)
        id-cita (:id (first cita))
        coleccion (library-manager-db/crear-coleccion state "Ciencias sociales" id-ref)
        id-col (:coleccion coleccion)
        biblioteca (library-manager-db/crear-biblioteca state id-u "Biblioteca Mayor Simón Bolívar" id-col)
        id-biblioteca (:biblioteca biblioteca)]
    (testing "Crea usuario"
      (is (uuid? id-u)))
    (testing "Actualiza usuario" 
      (is (= 1 (library-manager-db/actualizar-usuario state "cuenta" "juliamarin_90" id-u))))
    (testing "Crea autor"
      (is (uuid? id-au)))
    (testing "Actualiza autor"
      (is (= 1 (library-manager-db/actualizar-autor state :apellidos "Linarez Alcántara" id-au))))
    (testing "Crea referencia"
      (is (uuid? id-ref)))
    (testing "Actualiza referencia"
      (is (= 1 (library-manager-db/actualizar-referencia state :editorial "La Luz" id-ref))))
    (testing "Crea publicaciones"
      (is (= 1 (count (:publicaciones ref)))))
    (testing "Crea comentario"
      (is (uuid? id-com)))
    (testing "Actualiza comentario"
      (is (= 1 (library-manager-db/actualizar-comentario state :comentario "Comente lo que comente, siempre hago un comentario" id-com))))
    (testing "Crea cita"
      (is (uuid? id-cita)))
    (testing "Actualiza cita"
      (is (= 1 (library-manager-db/actualizar-cita state :paginas "19" id-cita))))
    (testing "Crear colección"
      (is (uuid? id-col)))
    (testing "Actualizar coleccion"
      (is (= 1 (library-manager-db/actualizar-coleccion state :nombre_coll "Coleccion nueva" id-col))))
    (testing "Crear biblioteca"
      (is (uuid? id-biblioteca)))
    (testing "Actualizar biblioteca"
      (is (= 1 (library-manager-db/actualizar-biblioteca state :nombre_biblioteca "Biblioteca de la Lora" id-biblioteca))))
    (testing "Eliminar biblioteca"
      (is (= 1 (library-manager-db/borrar-biblioteca state id-biblioteca))))
    (testing "Eliminar coleccion"
      (is (= 1 (library-manager-db/borrar-coleccion state id-col))))
    (testing "Elimina cita"
      (is (= 1 (library-manager-db/borrar-cita state id-cita))))
    (testing "Elimina comentario"
      (is (= 1 (library-manager-db/borrar-comentario state id-com))))
    (testing "Elimina referencia"
      (is (= 1 (library-manager-db/borrar-referencia state id-ref))))
    (testing "Elimina usuario" 
      (is (= 1 (library-manager-db/borrar-usuario state id-u))))
    (testing "Elimina autor"
      (is (= 1 (library-manager-db/borrar-autor state id-au))))))
 
   
(deftest pruebas-integracion-xtdb
  (let [state (second (:reitit.routes/api (utils/system-state)))
        user (library-manager-db/crear-usuario state "July Tovar" "julitovar@gmail.com" "julitcsa55" "dfd $$$ 22") 
        author (library-manager-db/crear-autor state "Juliana" "Marik") 
        ref (library-manager-db/crear-referencia state "Libro" "Clojure for Data Science" "2023" "XXX" "Madrid" nil nil nil [author]) 
        comentario (library-manager-db/crear-comentario state "Este es un comentario muy juicioso..." "123-156" "caras largas google dolar" ref user) 
        cita (library-manager-db/crear-cita state ref "Las caras largas son más largas que las largas" "120" user) 
        coleccion (library-manager-db/crear-coleccion state "Ciencias de la Computación" ref) 
        biblioteca (library-manager-db/crear-biblioteca state user "Biblioteca del Saber" coleccion)]
    (testing "Crea usuario"
      (is (uuid? user)))
    (testing "Actualiza usuario"
      (is (map? (library-manager-db/actualizar-usuario state "cuenta" "juliamarinppp" user))))
    (testing "Crea autor"
      (is (uuid? author)))
    (testing "Actualiza autor"
      (is (map? (library-manager-db/actualizar-autor state :apellidos "Franco Rivero" author))))
    (testing "Crea referencia"
      (is (uuid? ref)))
    (testing "Actualiza referencia"
      (is (map? (library-manager-db/actualizar-referencia state :editorial "Schumpeter & Bros" ref)))) 
    (testing "Crea comentario"
      (is (uuid? comentario)))
    (testing "Actualiza comentario"
      (is (map? (library-manager-db/actualizar-comentario state :comentario "Comente lo que comente, siempre hago un comentario" comentario))))
    (testing "Crea cita"
      (is (uuid? cita)))
    (testing "Actualiza cita"
      (is (map? (library-manager-db/actualizar-cita state :paginas "190" cita))))
    (testing "Crear colección"
      (is (uuid? coleccion)))
    (testing "Actualizar coleccion"
      (is (map? (library-manager-db/actualizar-coleccion state :nombre_coll "Coleccion vieja" coleccion))))
    (testing "Crear biblioteca"
      (is (uuid? biblioteca)))
    (testing "Actualizar biblioteca"
      (is (map? (library-manager-db/actualizar-biblioteca state :nombre_biblioteca "Biblioteca bibliófila" biblioteca))))
    (testing "Eliminar biblioteca"
      (is (instance? java.util.Date (library-manager-db/borrar-biblioteca state biblioteca))))
    (testing "Eliminar coleccion"
      (is (instance? java.util.Date (library-manager-db/borrar-coleccion state coleccion))))
    (testing "Elimina cita"
      (is (instance? java.util.Date (library-manager-db/borrar-cita state cita))))
    (testing "Elimina comentario"
      (is (instance? java.util.Date (library-manager-db/borrar-comentario state comentario))))
    (testing "Elimina referencia"
      (is (instance? java.util.Date (library-manager-db/borrar-referencia state ref))))
    (testing "Elimina usuario"
      (is (instance? java.util.Date (library-manager-db/borrar-usuario state user))))
    (testing "Elimina autor"
      (is (instance? java.util.Date (library-manager-db/borrar-autor state author)))))) 


(comment
  :dbg 
  (def state-xtdb (second (:reitit.routes/api (utils/system-state)))) 
  (library-manager-db/crear-usuario state-xtdb "Marco Tovar" "marcotovar@gmail.com" "marcossd_tcsa55" "d99fd $$$ 22")
  (xtdb/latest-completed-tx (:query-fn state-xtdb)) 
  (library-manager-db/crear-usuario (-> (utils/system-state) :router/routes first second) "Julia Marín" "juliamarin@gmail.com" "marin324" "Ksfws 434") 
  (tap> (run-test pruebas_integracion_sql))
  (run-tests)
  (run-test pruebas-integracion-xtdb)
  )
 