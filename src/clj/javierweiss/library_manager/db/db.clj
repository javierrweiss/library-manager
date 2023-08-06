(ns javierweiss.library-manager.db.db
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [integrant.repl.state :as state]
    [javierweiss.library-manager.datalog.documents :as datalog.documents]
    [javierweiss.library-manager.datalog.queries :as datalog.queries]))


;; CONFIGURACION ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod ig/init-key :db/conn [_ conn]
  conn)

(defmethod ig/init-key :db/testcontainers [_ _]
  nil)

;; FUNCIONES GENERALES ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti obtener-entidades (fn [state-map _ _] (:db-type state-map)))

(defmethod obtener-entidades :sql 
  [state-map tabla _] 
  ((:query-fn state-map) :obtener-todo {:table tabla}))

(defmethod obtener-entidades :xtdb
 [state-map _ entidad]
 (datalog.queries/obtener-todas-las-entidades (:query-fn state-map) entidad))

(defmulti obtener-entidad-por-id (fn [state-map _ _] (:db-type state-map)))

(defmethod obtener-entidad-por-id :sql
  [state-map tabla id]
  ((:query-fn state-map) :obtener-por-id {:table tabla
                                          :id id}))

(defmethod obtener-entidad-por-id :xtdb
  [state-map _ id]
  (datalog.queries/obtener-por-id (:query-fn state-map) id))

(defmulti actualizar-entidad (fn [state-map _ _ _ _] (:db-type state-map)))

(defmethod actualizar-entidad :sql
  [state-map tabla campo valor id]
  (let [campo (keyword campo)]
    ((:query-fn state-map) :actualizar-registro! {:table (str tabla "s")  
                                                  :updates {campo valor}
                                                  :id id})))

(defmethod actualizar-entidad :xtdb
  [state-map tabla campo valor id]
  (let [campo (keyword tabla campo)]
    (datalog.queries/actualizar-entidad (:query-fn state-map) id campo valor)))

(defmulti borrar-entidad (fn [state-map _ _] (:db-type state-map)))

(defmethod borrar-entidad :sql
  [state-map tabla id]
  ((:query-fn state-map) :borrar-por-id! {:table tabla
                                          :id id}))

(defmethod borrar-entidad :xtdb
 [state-map _ id]
 (datalog.queries/borrar-por-id (:query-fn state-map) id))

;; USUARIOS ;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti crear-usuario (fn [state-map _ _ _ _] (:db-type state-map)))

(defmethod crear-usuario :sql
  [state-map nombre correo cuenta clave]
  ((:query-fn state-map) :crear-usuario! {:usuarios/nombre nombre
                                          :usuarios/correo correo
                                          :usuarios/cuenta cuenta
                                          :usuarios/clave clave}))

(defmethod crear-usuario :xtdb
 [state-map nombre correo cuenta clave]
 (datalog.queries/agregar-doc (:query-fn state-map) (datalog.documents/crear-doc-usuario! nombre correo cuenta clave)))

(defn obtener-usuarios
  [state-map] 
  (obtener-entidades state-map "usuarios" :usuario/nombre))

(defn obtener-usuario-por-id
  [state-map id] 
  (obtener-entidad-por-id state-map "usuarios" id))

(defn actualizar-usuario
  [state-map campo valor id] 
  (actualizar-entidad  state-map "usuario" campo valor id))

(defn borrar-usuario
  [state-map id] 
  (borrar-entidad state-map "usuarios" id))


;; AUTORES ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-autor
  "qfn => Query function \n
   nombres, apellidos => string"
  [db-type qfn nombres apellidos]
  (if (= db-type "xtdb")
    (datalog.queries/agregar-doc qfn (datalog.documents/crear-doc-autor! nombres apellidos))
    (qfn :crear-autor! {:autores/nombres nombres
                        :autores/apellidos apellidos})))


(defn obtener-autores
  [db-type qfn]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-todos-autores qfn)
    (obtener-entidades db-type qfn "autores" :autor/nombres)))


(defn obtener-autor-por-id
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-por-id qfn id)
    (obtener-entidad-por-id db-type qfn "autores" id)))


(defn actualizar-autor
  [db-type qfn campo valor id]
  (if (= db-type "xtdb")
    (datalog.queries/actualizar-entidad qfn id campo valor)
    (actualizar-entidad db-type qfn "autores" campo valor id)))


(defn borrar-autor
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/borrar-por-id qfn id)
    (borrar-entidad db-type qfn "autores" id)))


;; PUBLICACIONES ;;;;;;;;;;;;;;;;;;;;;;

(defn- crear-publicacion
  [qfn referencia autor]
  (qfn :crear-publicacion! {:publicaciones/referencia referencia
                            :publicaciones/autor autor}))


(defn- obtener-publicaciones
  [qfn]
  (qfn :obtener-todo {:table "publicaciones"}))


(defn- obtener-publicacion-por-id
  [qfn id]
  (qfn :obtener-por-id {:table "publicaciones"
                        :id id}))


(defn- actualizar-publicacion
  [qfn campo valor id]
  (let [campo (if-not (keyword? campo) (keyword campo) campo)]
    (qfn :actualizar-registro! {:table "publicaciones"
                                :updates {campo valor}
                                :id id})))


;; ¿La necesito?
#_(defn borrar-publicacion
  [qfn id]
  (qfn :borrar-por-id! {:table "publicaciones"
                        :id id}))


;; REFERENCIAS ;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-referencia
  "qfn => Query function \n
   tipo_publicacion,titulo,ciudad,ano,editorial,ciudad, volumen, nombre_revista, nombre_libro => string \n
   autores => [uuid?]"
  [db-type qfn tipo_publicacion titulo ano editorial ciudad volumen nombre_revista nombre_libro autores]
  (if (= db-type "xtdb")
    (datalog.queries/agregar-doc qfn
                                 (datalog.documents/crear-doc-referencia! tipo_publicacion titulo ano editorial ciudad volumen nombre_revista nombre_libro autores))
    (let [ref-id (-> (qfn :crear-referencia! {:referencia/autores autores
                                              :referencia/titulo titulo
                                              :referencia/ano ano
                                              :referencia/editorial editorial
                                              :referencia/ciudad ciudad
                                              :referencia/tipo_publicacion tipo_publicacion
                                              :referencia/volumen volumen
                                              :referencia/nombre_libro nombre_libro
                                              :referencia/nombre_revista nombre_revista})
                     first
                     :id)
          pubs-ids (into [] (for [autor autores]
                              (-> (crear-publicacion qfn ref-id autor)
                                  first
                                  :id)))]
      {:referencia ref-id
       :publicaciones pubs-ids})))


(defn obtener-referencias
  [db-type qfn]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-todas-referencias qfn)
    (obtener-entidades db-type qfn "referencias" :referencia/titulo)))


(defn obtener-referencia-por-id
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-por-id qfn id)
    (obtener-entidad-por-id db-type qfn "referencias" id)))


(defn actualizar-referencia
  [db-type qfn campo valor id]
  (let [campo (if-not (keyword? campo) (keyword campo) campo)
        actualiza-ref-sql (fn []
                            (qfn :actualizar-registro! {:table "referencias"
                                                        :updates {campo valor}
                                                        :id id}))]
    (cond
      (= db-type "xtdb") (datalog.queries/actualizar-entidad qfn id campo valor)
      (= campo :autor) (->> (actualiza-ref-sql)
                            (actualizar-publicacion qfn campo valor))
      :else (actualiza-ref-sql))))


(defn borrar-referencia
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/borrar-por-id qfn id)
    (borrar-entidad db-type qfn "referencias" id)))


;; CITAS ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-cita
  "qfn => Query function \n
   referencia => uuid \n
   cita, paginas => string \n
   usuario => uuid"
  [db-type qfn referencia cita paginas usuario]
  (if (= db-type "xtdb")
    (datalog.queries/agregar-doc qfn (datalog.documents/crear-doc-citas! referencia cita paginas usuario))
    (qfn :crear-cita! {:citas/referencia referencia
                       :citas/cita cita
                       :citas/paginas paginas
                       :citas/usuario usuario})))


(defn obtener-citas
  [db-type qfn]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-todas-citas qfn)
    (obtener-entidades db-type qfn "citas" :cita/referencia)))


(defn obtener-cita-por-id
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-por-id qfn id)
    (obtener-entidad-por-id db-type qfn "citas" id)))


(defn actualizar-cita
  [db-type qfn campo valor id]
  (if (= db-type "xtdb")
    (datalog.queries/actualizar-entidad qfn id campo valor)
    (actualizar-entidad db-type qfn "citas" campo valor id)))


(defn borrar-cita
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/borrar-por-id qfn id)
    (borrar-entidad db-type qfn "citas" id)))


;; COMENTARIOS ;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-comentario
  "qfn => Query function \n
   comentario, paginas, palabras_clave => string \n
   referencia, usuario => uuid"
  [db-type qfn comentario paginas palabras_clave referencia usuario]
  (if (= db-type "xtdb")
    (datalog.queries/agregar-doc qfn (datalog.documents/crear-doc-comentarios! referencia comentario paginas palabras_clave usuario))
    (qfn :crear-comentario! {:comentarios/referencia referencia
                             :comentarios/comentario comentario
                             :comentarios/paginas paginas
                             :comentarios/palabras_clave palabras_clave
                             :comentarios/usuario usuario})))

(defn obtener-comentarios
  [db-type qfn]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-todos-comentarios qfn)
    (obtener-entidades db-type qfn "comentarios" :comentario/referencia)))


(defn obtener-comentario-por-id
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-por-id qfn id)
    (obtener-entidad-por-id db-type qfn "comentarios" id)))


(defn actualizar-comentario
  [db-type qfn campo valor id]
  (if (= db-type "xtdb")
    (datalog.queries/actualizar-entidad qfn id campo valor)
    (actualizar-entidad db-type qfn "comentarios" campo valor id)))


(defn borrar-comentario
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/borrar-por-id qfn id)
    (borrar-entidad db-type qfn "comentarios" id)))


;; COLECCION-ITEMS ::::::::::::::::::::::::::

(defn- crear-coleccion-item
  [qfn coleccion referencia]
  (qfn :crear-item-coleccion! {:coleccion_items/coleccion coleccion
                               :coleccion_items/referencia referencia}))


(defn- obtener-coleccion-items
  [qfn]
  (qfn :obtener-todo {:table "coleccion_items"}))


(defn- obtener-coleccion-item-por-id
  [qfn id]
  (qfn :obtener-por-id {:table "coleccion_items"
                        :id id}))


(defn- actualizar-coleccion-items
  [qfn campo valor id]
  (let [campo (if-not (keyword? campo) (keyword campo) campo)]
    (qfn :actualizar-registro! {:table "coleccion-items"
                                :updates {campo valor}
                                :id id})))


(defn- borrar-coleccion-items
  [qfn id]
  (qfn :borrar-por-id! {:table "coleccion_items"
                        :id id}))


;; COLECCIONES ;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-coleccion
  "qfn => Query function \n
   nombre_coll => string \n
   referencias => uuid"
  [db-type qfn nombre_coll & referencias]
  (if (= db-type "xtdb")
    (datalog.queries/agregar-doc qfn (datalog.documents/crear-doc-colecciones! nombre_coll referencias))
    (let [coll-id (-> (qfn :crear-coleccion! {:colecciones/nombre_coll nombre_coll})
                      first
                      :id)
          items-ids (into [] (for [referencia referencias] (-> (crear-coleccion-item qfn coll-id referencia)
                                                               first
                                                               :id)))]
      {:coleccion coll-id
       :items items-ids})))


(defn obtener-colecciones
  [db-type qfn]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-todas-colecciones qfn)
    (obtener-entidades db-type qfn "colecciones" :coleccion/nombre_coll)))


(defn obtener-coleccion-por-id
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-por-id qfn id)
    (obtener-entidad-por-id db-type qfn "coleccciones" id)))


(defn actualizar-coleccion
  [db-type qfn campo valor id]
  (if (= db-type "xtdb")
    (datalog.queries/actualizar-entidad qfn id campo valor)
    (actualizar-entidad db-type qfn "colecciones" campo valor id)))


(defn borrar-coleccion
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/borrar-por-id qfn id)
    (borrar-entidad db-type qfn "colecciones" id)))


;; BIBLIOTECA-ITEMS ;;;;;;;;;;;;;;;;;;;;;;;;

(defn- crear-item-biblioteca
  [qfn biblioteca coleccion]
  (qfn :crear-item-biblioteca! {:biblioteca_items/biblioteca biblioteca
                                :biblioteca_items/coleccion coleccion}))


(defn- obtener-biblioteca-items
  [qfn]
  (qfn :obtener-todo {:table "biblioteca-items"}))


(defn- obtener-biblioteca-item-por-id
  [qfn id]
  (qfn :obtener-por-id {:table "biblioteca-items"
                        :id id}))


(defn- actualizar-biblioteca-item
  [qfn campo valor id]
  (let [campo (if-not (keyword? campo) (keyword campo) campo)]
    (qfn :actualizar-registro! {:table "biblioteca-items"
                                :updates {campo valor}
                                :id id})))


(defn- borrar-biblioteca-item
  [qfn id]
  (qfn :borrar-por-id! {:table "biblioteca-items"
                        :id id}))


;; BIBLIOTECAS ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-biblioteca
  "qfn => Query function \n
   usuario => uuid \n
   nombre_biblioteca => string \n
   colecciones => uuid"
  [db-type qfn usuario nombre_biblioteca & colecciones]
  (if (= db-type "xtdb")
    (datalog.queries/agregar-doc qfn (datalog.documents/crear-doc-bibliotecas! usuario nombre_biblioteca (vec colecciones)))
    (let [bib-id (-> (qfn :crear-biblioteca! {:bibliotecas/nombre_biblioteca nombre_biblioteca
                                              :bibliotecas/usuario usuario})
                     first
                     :id)
          colecciones-ids (for [coleccion colecciones]
                            (-> (crear-item-biblioteca qfn bib-id coleccion)
                                first
                                :id))]
      {:biblioteca bib-id
       :colecciones colecciones-ids})))


(defn obtener-bibliotecas
  [db-type qfn]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-todas-bibliotecas qfn)
    (obtener-entidades db-type qfn "bibliotecas" :coleccion/nombre_coll)))


(defn obtener-biblioteca-por-id
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/obtener-por-id qfn id)
    (obtener-entidad-por-id db-type qfn "bibliotecas" id)))


(defn actualizar-biblioteca
  [db-type qfn campo valor id]
  (if (= db-type "xtdb")
    (datalog.queries/actualizar-entidad qfn id campo valor)
    (actualizar-entidad db-type qfn "bibliotecas" campo valor id)))


(defn borrar-biblioteca
  [db-type qfn id]
  (if (= db-type "xtdb")
    (datalog.queries/borrar-por-id qfn id)
    (borrar-entidad db-type qfn "bibliotecas" id)))


;; OTRAS CONSULTAS ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(comment
  :tut/basics
  :tut/next
  :dbg
  (keys state/system)
  (:system/env state/system)
  (def q (-> (state/system [:db/conn :db-type/xtdb]) :conn))
  (def q-sql (-> (state/system [:db/conn :db-type/sql]) :conn))
  (tap> (obtener-usuarios "sql" q-sql))
  (actualizar-usuario "sql" q-sql "correo" "mireya_sarda@gmail.com" #uuid "eaf55b4a-f37e-420d-9b78-55a99f46a703")
  (actualizar-usuario "xtdb" q "nombre" "Julio Vargas" #uuid "ea601669-c7e0-404e-bcff-02a13753480c")
  (type q)
  (tap> (obtener-usuarios "xtdb" q))

  (actualizar-usuario "xtdb" q :nombre "Miguel Marin" #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b")
  (obtener-usuario-por-id "xtdb" q #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b")
  (obtener-usuario-por-id "xtdb" q #uuid "46b37e5c-5242-4ea5-a963-46b4ca7ccaf1")
  (borrar-usuario "xtdb" q #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b")
  (crear-usuario "xtdb" q "José Marín" "marino@gmail.com" "el_marine" "232jk sds **")
  (obtener-autores "xtdb" q)
  (obtener-autor-por-id "xtdb" q #uuid "961469a7-ff35-47a7-b136-9a807a7666a9")
  (actualizar-autor "xtdb" q :nombres "Julian Amado" #uuid "961469a7-ff35-47a7-b136-9a807a7666a9")
  (borrar-autor "xtdb" q #uuid "961469a7-ff35-47a7-b136-9a807a7666a9")
  (crear-autor "xtdb" q "Julio César" "Fabregas")
  (obtener-referencias "xtdb" q)
  (obtener-referencia-por-id "xtdb" q #uuid "966e1a6a-b667-4fb2-bd8b-56372ff99809")
  (actualizar-referencia "xtdb" q :ano 2009 #uuid "966e1a6a-b667-4fb2-bd8b-56372ff99809")
  (borrar-referencia "xtdb" q #uuid "966e1a6a-b667-4fb2-bd8b-56372ff99809")
  (crear-referencia "xtdb" q "Libro" "Los años de Valcarce" "2009" "El Farolito" "Madrid" nil nil nil [#uuid "ec9d24d5-2ca1-45d3-b97b-32d79c6619ba"])
  :ex
  (tap>
   (crear-coleccion q "Coleccion Animal" #uuid "dc79fd79-e872-448b-b1bf-4b0bd1c2f748"))
  (crear-coleccion q "Coleccion horizontes" #uuid "dc79fd79-e872-448b-b1bf-4b0bd1c2f748" (java.util.UUID/randomUUID))
  (obtener-colecciones "xtdb" q)
  (obtener-publicaciones  q)
  (obtener-citas "xtdb" q)
  (obtener-comentarios "xtdb" q)
  (into [] (for [autor [#uuid "2317eb29-30c0-4c7a-9b90-99ccdad4b0f4"
                        #uuid "97d8cfe9-f260-4657-b483-05ad52d6a5eb"
                        #uuid "9f0a9109-cb33-4765-93e7-87d0f92cd838"]]
             (-> (crear-publicacion q #uuid "abb6620f-3746-4680-9969-2f1b96fe16e5" autor)
                 first
                 :id)))
  (datalog.documents/crear-doc-bibliotecas! (java.util.UUID/randomUUID) "Biblioteca mayor" (java.util.UUID/randomUUID) (java.util.UUID/randomUUID))
  (datalog.documents/crear-doc-colecciones! "Coleccion coleccionable" (java.util.UUID/randomUUID))
  (xtdb.api/attribute-stats datalog.queries/node)

  (ns-unmap *ns* 'get-entity)
 
  (defmulti get-entity (fn [state-map _ _] (:db-type state-map)))
    
  (defmethod get-entity :sql
    [state-map tabla _]
    ((:query-fn state-map) :obtener-todo {:table tabla}))
  
  (defmethod get-entity :xtdb
    [state-map _ entidad]
    (datalog.queries/obtener-todas-las-entidades (:query-fn state-map) entidad))
  
  (get-entity (-> state/system :reitit.routes/api second) "" :usuario/nombre)
  )   
   