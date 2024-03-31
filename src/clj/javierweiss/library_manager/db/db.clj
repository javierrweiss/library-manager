(ns javierweiss.library-manager.db.db
  (:require 
   [integrant.core :as ig]
   [integrant.repl.state :as state]
   [javierweiss.library-manager.datalog.documents :as datalog.documents]
   [javierweiss.library-manager.datalog.queries :as datalog.queries]
   [clojure.string :as s]))

;; CONFIGURACION ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod ig/init-key :db/type [_ type]
  type)

(defmethod ig/init-key :db/testcontainers [_ container]
  container)

;; FUNCIONES GENERALES ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti obtener-entidades (fn [state-map _ _] (:db-type state-map)))

(defmethod obtener-entidades :sql 
  [state-map tabla _] 
  ((:query-fn state-map) :obtener-todo {:table tabla}))

(defmethod obtener-entidades :xtdb
 [state-map _ entidad]
 (datalog.queries/obtener-todas-las-entidades (:query-fn state-map) entidad))

(defmethod obtener-entidades :default
   [_ _ _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defmulti obtener-entidad-por-id (fn [state-map _ _] (:db-type state-map)))

(defmethod obtener-entidad-por-id :sql
  [state-map tabla id]
  ((:query-fn state-map) :obtener-por-id {:table tabla
                                          :id id}))

(defmethod obtener-entidad-por-id :xtdb
  [state-map _ id]
  (datalog.queries/obtener-por-id (:query-fn state-map) id))

(defmethod obtener-entidad-por-id :default
  [_ _ _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defmulti actualizar-entidad (fn [state-map _ _ _ _] (:db-type state-map)))

(defmethod actualizar-entidad :sql
  [state-map tabla campo valor id]
  (let [campo (keyword campo)]
    ((:query-fn state-map) :actualizar-registro! {:table (if (or (s/ends-with? tabla "n") (s/ends-with? tabla "r")) (str tabla "es") (str tabla "s"))  
                                                  :updates {campo valor}
                                                  :id id})))

(defmethod actualizar-entidad :xtdb
  [state-map tabla campo valor id]
  (let [t (if (keyword? tabla) (-> tabla symbol str) tabla)
        c (if (keyword? campo) (-> campo symbol str) campo)
        campo (keyword t c)]
    (datalog.queries/actualizar-entidad (:query-fn state-map) id campo valor)))

(defmethod actualizar-entidad :default
  [_ _ _ _ _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defmulti borrar-entidad (fn [state-map _ _] (:db-type state-map)))

(defmethod borrar-entidad :sql
  [state-map tabla id]
  ((:query-fn state-map) :borrar-por-id! {:table tabla
                                          :id id}))

(defmethod borrar-entidad :xtdb
 [state-map _ id]
 (datalog.queries/borrar-por-id (:query-fn state-map) id))

(defmethod borrar-entidad :default
  [_ _ _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

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

(defmethod crear-usuario :default
  [_ _ _ _ _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defn obtener-usuarios
  [state-map] 
  (obtener-entidades state-map "usuarios" :usuario/nombre))

(defn obtener-usuario-por-id
  [state-map id] 
  (obtener-entidad-por-id state-map "usuarios" id))


(defmulti obtener-usuario (fn [state-map _ _ ] (:db-type state-map)))

(defmethod obtener-usuario :sql
  [state-map cuenta clave] 
  {:pre [(bytes? clave)]}
  ((:query-fn state-map) :buscar-usuario {:usuarios/cuenta cuenta
                                          :usuarios/clave clave}))

(defmethod obtener-usuario :xtdb
  [state-map cuenta clave]
  {:pre [(bytes? clave)]}
  (datalog.queries/obtener-usuario-por-cuenta (:query-fn state-map) cuenta clave))

(defmethod obtener-usuario :default
  [_ _ _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defn actualizar-usuario
  [state-map campo valor id] 
  (actualizar-entidad  state-map "usuario" campo valor id))

(defn borrar-usuario
  [state-map id] 
  (borrar-entidad state-map "usuarios" id))


;; AUTORES ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti crear-autor (fn [state-map _ _] (:db-type state-map)))

(defmethod crear-autor :sql 
  [state-map nombres apellidos]
  ((:query-fn state-map) :crear-autor! {:autores/nombres nombres
                                        :autores/apellidos apellidos}))

(defmethod crear-autor :xtdb
  [state-map nombres apellidos]
  (datalog.queries/agregar-doc (:query-fn state-map) (datalog.documents/crear-doc-autor! nombres apellidos)))

(defmethod crear-autor :default
  [_ _ _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defn obtener-autores
  [state-map] 
  (obtener-entidades state-map "autores" :autor/nombres))

(defn obtener-autor-por-id
  [state-map id] 
  (obtener-entidad-por-id state-map "autores" id))

(defn actualizar-autor
  [state-map campo valor id] 
  (actualizar-entidad state-map "autor" campo valor id))

(defn borrar-autor
  [state-map id] 
  (borrar-entidad state-map "autores" id))


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

(defn- borrar-publicacion
  [qfn id]
  (qfn :borrar-por-id! {:table "publicaciones"
                        :id id}))

;; REFERENCIAS ;;;;;;;;;;;;;;;;;;;;;;;

(defmulti crear-referencia (fn [state-map _ _ _ _ _ _ _ _ _] (:db-type state-map)))

(defmethod crear-referencia :sql
  [state-map tipo_publicacion titulo ano editorial ciudad volumen nombre_revista nombre_libro autores]
  (let [qfn (:query-fn state-map)
        ref-id (-> (qfn :crear-referencia! {:referencia/titulo titulo
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
     :publicaciones pubs-ids}))

(defmethod crear-referencia :xtdb
  [state-map tipo_publicacion titulo ano editorial ciudad volumen nombre_revista nombre_libro autores]
  (datalog.queries/agregar-doc
   (:query-fn state-map)
   (datalog.documents/crear-doc-referencia! tipo_publicacion titulo ano editorial ciudad volumen nombre_revista nombre_libro autores)))

(defmethod crear-referencia :default
  [_ _ _ _ _ _ _ _ _ _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defn obtener-referencias
  [state-map] 
  (obtener-entidades state-map "referencias" :referencia/titulo))

(defn obtener-referencia-por-id
  [state-map id] 
  (obtener-entidad-por-id state-map "referencias" id))

(defmulti actualizar-referencia (fn [state-map _ _ _] (:db-type state-map)))

(defmethod actualizar-referencia :sql
  [state-map campo valor id]
  (let [qfn (:query-fn state-map)
        campo (if-not (keyword? campo) (keyword campo) campo)
        actualiza-ref-sql (fn []
                            (qfn :actualizar-registro! {:table "referencias"
                                                        :updates {campo valor}
                                                        :id id}))]
    (cond
      (= campo :autor) (->> (actualiza-ref-sql)
                            (actualizar-publicacion qfn campo valor))
      :else (actualiza-ref-sql))))

(defmethod actualizar-referencia :xtdb
 [state-map campo valor id]
 (datalog.queries/actualizar-entidad (:query-fn state-map) id campo valor))

(defmethod actualizar-referencia :default
  [_ _ _ _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defn borrar-referencia
  [state-map id] 
  (borrar-entidad state-map "referencias" id))

;; CITAS ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti crear-cita (fn [state-map _ _ _ _] (:db-type state-map)))

(defmethod crear-cita :sql
  [state-map referencia cita paginas usuario]
  ((:query-fn state-map) :crear-cita! {:citas/referencia referencia
                                       :citas/cita cita
                                       :citas/paginas paginas
                                       :citas/usuario usuario}))
(defmethod crear-cita :xtdb
 [state-map referencia cita paginas usuario]
 (datalog.queries/agregar-doc (:query-fn state-map) (datalog.documents/crear-doc-citas! referencia cita paginas usuario)))

(defmethod crear-cita :default
  [_ _ _ _ _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defn obtener-citas
  [state-map] 
  (obtener-entidades state-map "citas" :cita/referencia))

(defn obtener-cita-por-id
  [state-map id] 
  (obtener-entidad-por-id state-map "citas" id))

(defn actualizar-cita
  [state-map campo valor id] 
  (actualizar-entidad state-map "cita" campo valor id))

(defn borrar-cita
  [state-map id] 
  (borrar-entidad state-map "citas" id))


;; COMENTARIOS ;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti crear-comentario (fn [state-map _ _ _ _ _] (:db-type state-map)))

(defmethod crear-comentario :sql
  [state-map comentario paginas palabras_clave referencia usuario]
  ((:query-fn state-map) :crear-comentario! {:comentarios/referencia referencia
                                             :comentarios/comentario comentario
                                             :comentarios/paginas paginas
                                             :comentarios/palabras_clave palabras_clave
                                             :comentarios/usuario usuario}))

(defmethod crear-comentario :xtdb
  [state-map comentario paginas palabras_clave referencia usuario]
  (datalog.queries/agregar-doc (:query-fn state-map) (datalog.documents/crear-doc-comentarios! referencia comentario paginas palabras_clave usuario)))

(defmethod crear-comentario :default
  [_ _ _ _ _ _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defn obtener-comentarios
  [state-map] 
  (obtener-entidades state-map "comentarios" :comentario/referencia))

(defn obtener-comentario-por-id
  [state-map id] 
  (obtener-entidad-por-id state-map "comentarios" id))

(defn actualizar-comentario
  [state-map campo valor id] 
  (actualizar-entidad state-map "comentario" campo valor id))

(defn borrar-comentario
  [state-map id] 
  (borrar-entidad state-map "comentarios" id))

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

(defmulti crear-coleccion (fn [state-map _ _] (:db-type state-map)))

(defmethod crear-coleccion :sql
  [state-map nombre_coll & referencias]
  (let [qfn (:query-fn state-map)
        coll-id (-> (qfn :crear-coleccion! {:colecciones/nombre_coll nombre_coll})
                    first
                    :id)
        items-ids (into [] (for [referencia referencias] (-> (crear-coleccion-item qfn coll-id referencia)
                                                             first
                                                             :id)))]
    {:coleccion coll-id
     :items items-ids}))

(defmethod crear-coleccion :xtdb
 [state-map nombre_coll & referencias]
 (datalog.queries/agregar-doc (:query-fn state-map) (datalog.documents/crear-doc-colecciones! nombre_coll referencias)))

(defmethod crear-coleccion :default
  [_ _ & _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defn obtener-colecciones
  [state-map] 
  (obtener-entidades state-map "colecciones" :coleccion/nombre_coll))

(defn obtener-coleccion-por-id
  [state-map id] 
  (obtener-entidad-por-id state-map "coleccciones" id))

(defn actualizar-coleccion
  [state-map campo valor id] 
  (actualizar-entidad state-map "coleccion" campo valor id))
  
(defn borrar-coleccion
  [state-map id] 
  (borrar-entidad state-map "colecciones" id))


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

(defmulti crear-biblioteca (fn [state-map _ _ _] (:db-type state-map)))

(defmethod crear-biblioteca :sql
 [state-map usuario nombre_biblioteca & colecciones]
 (let [qfn (:query-fn state-map)
       bib-id (-> (qfn :crear-biblioteca! {:bibliotecas/nombre_biblioteca nombre_biblioteca
                                           :bibliotecas/usuario usuario})
                  first
                  :id)
       colecciones-ids (for [coleccion colecciones]
                         (-> (crear-item-biblioteca qfn bib-id coleccion)
                             first
                             :id))]
   {:biblioteca bib-id
    :colecciones colecciones-ids}))

(defmethod crear-biblioteca :xtdb
 [state-map usuario nombre_biblioteca & colecciones]
 (datalog.queries/agregar-doc (:query-fn state-map) (datalog.documents/crear-doc-bibliotecas! usuario nombre_biblioteca colecciones)))

(defmethod crear-biblioteca :default
  [_ _ _ & _]
  (throw (IllegalArgumentException. "Opción no implementada. Atomo del sistema nulo, posiblemente no arrancado")))

(defn obtener-bibliotecas
  [state-map] 
  (obtener-entidades state-map "bibliotecas" :coleccion/nombre_coll))

(defn obtener-biblioteca-por-id
  [state-map id] 
  (obtener-entidad-por-id state-map "bibliotecas" id))

(defn actualizar-biblioteca
  [state-map campo valor id] 
  (actualizar-entidad state-map "biblioteca" campo valor id))

(defn borrar-biblioteca
  [state-map id] 
  (borrar-entidad state-map "bibliotecas" id))

;; OTRAS CONSULTAS ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(comment
  (keyword :er)
  (keyword "qw" "df")
  :tut/basics
  :tut/next 
  :dbg
  (tap> state/system)
  (:db/db state/system)
  (:system/env state/system)
  (keys (:reitit.routes/api state/system))  
  (def q (-> (:router/routes state/system) second second)) 
  (tap> q)
  (def q-sql (assoc (second (:reitit.routes/api state/system))
                    :query-fn (:db.sql/query-fn state/system) 
                    :db-type :sql))
  (obtener-usuarios q)
  (obtener-usuario q "juanmora" "dssdr333")
  (def u (crear-usuario q "Juana Mann" "juanamann@gmail.com" "jmann" "4645p*454"))
  (actualizar-usuario q "nombre" "Mario Mariana" #uuid "1b59d86a-dea9-45b2-84c4-9a67677e7271")
  (crear-autor q-sql "Juana" "Mariana") 
  (crear-referencia q-sql 
                    "Libro" 
                    "Los años de Valcarce" 
                    "2009" 
                    "El Farolito" 
                    "Madrid" 
                    nil 
                    nil
                    nil
                    [#uuid "4152e1ae-2a9e-4720-af20-3fcd1ff8d275"])
  (try
    ((:db.sql/query-fn state/system) :crear-referencia!  {:referencia/titulo "Las arpas"
                                                          :referencia/ano "2021"
                                                          :referencia/editorial "Aienadas S.A."
                                                          :referencia/ciudad "Caracas"
                                                          :referencia/tipo_publicacion "Libro"
                                                          :referencia/volumen nil
                                                          :referencia/nombre_libro "Las carpas"
                                                          :referencia/nombre_revista nil})
    (catch java.sql.SQLException e (println (.getMessage e))))
  
  (:crear-usuario! (:db.sql/query-fn state/system) {:usuario/correo "jrivero@gmail.com"
                                                    :usuario/cuenta "jrivero"
                                                    :usuario/nombre "Juliana Rivero"
                                                    :usuario/clave "Mi super keyword"})
  (type (obtener-usuarios q))
  (obtener-usuarios state/system)
  (obtener-usuarios q-sql)
  (obtener-autores q-sql)


  (tap> (obtener-referencias q-sql))
  (count (obtener-referencias q-sql))

  :ex
  (tap>
   (crear-coleccion q "Coleccion Animal" #uuid "dc79fd79-e872-448b-b1bf-4b0bd1c2f748"))
  (crear-coleccion q "Coleccion horizontes" #uuid "dc79fd79-e872-448b-b1bf-4b0bd1c2f748" (java.util.UUID/randomUUID))
  (obtener-publicaciones  q)
  (into [] (for [autor [#uuid "2317eb29-30c0-4c7a-9b90-99ccdad4b0f4"
                        #uuid "97d8cfe9-f260-4657-b483-05ad52d6a5eb"
                        #uuid "9f0a9109-cb33-4765-93e7-87d0f92cd838"]]
             (-> (crear-publicacion q #uuid "abb6620f-3746-4680-9969-2f1b96fe16e5" autor)
                 first
                 :id)))
  (datalog.documents/crear-doc-bibliotecas! (java.util.UUID/randomUUID) "Biblioteca mayor" (java.util.UUID/randomUUID) (java.util.UUID/randomUUID))
  (datalog.documents/crear-doc-colecciones! "Coleccion coleccionable" (java.util.UUID/randomUUID))

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
   