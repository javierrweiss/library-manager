(ns javierweiss.library-manager.db.db
  (:require [javierweiss.library-manager.datalog.queries :as datalog.queries]
            [javierweiss.library-manager.datalog.schema :as datalog.schema]
            [integrant.core :as ig]
            [integrant.repl.state :as state]
            [clojure.tools.logging :as log]))

(defmethod ig/init-key :db/type [_ {:keys [db-type]}] db-type) 
 
(def ^:dynamic *db-type* (:db/type state/system))

;; REVISAR TODAS LAS OPERACIONES!!! NO SON IGUALES LOS ESQUEMAS DE SQL Y DATALOG

(defn- obtener-entidades
  "Recibe un argumento tipo string para la tabla SQL y un argumento tipo keyword para las entidades Datalog"
  [qfn tabla entidad]
  (log/info "Ejecutando sentencia en: " *db-type*)
  (if (= *db-type* "xtdb")
    (datalog.queries/obtener-todas-las-entidades datalog.queries/node entidad)
    (qfn :obtener-todo {:table tabla})))

(defn- obtener-entidad-por-id
  "Nombre de la tabla para SQL e id de la entidad para ambos modos de persistencia"
  [qfn tabla id]
  (log/info "Ejecutando sentencia en: " *db-type*)
  (if (= *db-type* "xtdb")
    (datalog.queries/obtener-por-id datalog.queries/node id)
    (qfn :obtener-por-id {:table tabla
                          :id id})))

(defn- actualizar-entidad
  [qfn tabla campo valor id]
  (log/info "Ejecutando sentencia en: " *db-type*)
  (let [campo (if-not (keyword? campo) (keyword campo) campo)]
    (if (= *db-type* "xtdb")
      (datalog.queries/actualizar-entidad datalog.queries/node id campo valor)
      (qfn :actualizar-registro! {:table tabla
                                  :updates {campo valor}
                                  :id id}))))

(defn- borrar-entidad
  [qfn tabla id]
  (log/info "Ejecutando sentencia en: " *db-type*)
  (if (= *db-type* "xtdb")
    (datalog.queries/borrar-por-id datalog.queries/node id)
    (qfn :borrar-por-id! {:table tabla
                          :id id})))

;;;;;;;;;;;;;;;;;;;;; USUARIOS ;;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-usuario
  "qfn => Query function \n
   nombre, correo, cuenta, clave => string"
  [qfn nombre correo cuenta clave]
  (if (= *db-type* "xtdb")
    (datalog.schema/crear-doc-usuario! nombre correo cuenta clave)
    (qfn :crear-usuario! {:usuarios/nombre nombre
                          :usuarios/correo correo
                          :usuarios/cuenta cuenta
                          :usuarios/clave clave})))

(defn obtener-usuarios
  [qfn]
  (obtener-entidades qfn "usuarios" :usuario/nombre))

(defn obtener-usuario-por-id
  [qfn id]
  (obtener-entidad-por-id qfn "usuarios" id))

(defn actualizar-usuario
  [qfn campo valor id]
  (actualizar-entidad qfn "usuarios" campo valor id))

(defn borrar-usuario
  [qfn id]
  (borrar-entidad qfn "usuarios" id))

;;;;;;;;;;;;;;;;;;;;;;; AUTORES ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-autor
  "qfn => Query function \n
   nombres, apellidos => string"
  [qfn nombres apellidos]
  (if (= *db-type* "xtdb")
    (datalog.schema/crear-doc-autor! nombres apellidos)
    (qfn :crear-autor! {:autores/nombres nombres
                        :autores/apellidos apellidos})))

(defn obtener-autores
  [qfn]
  (obtener-entidades qfn "autores" :autor/nombres))

(defn obtener-autor-por-id
  [qfn id]
  (obtener-entidad-por-id qfn "autores" id))

(defn actualizar-autor
  [qfn campo valor id]
  (actualizar-entidad qfn "autores" campo valor id))

(defn borrar-autor 
  [qfn id]
  (borrar-entidad qfn "autores" id))

;;;;;;;;;;;;;;;;;;;;;;;;;;; PUBLICACIONES ;;;;;;;;;;;;;;;;;;;;;;

(defn- crear-publicacion
  [qfn referencia autor]
  (qfn :crear-publicacion! {:publicaciones/referencia referencia
                            :publicaciones/autor autor}))

(defn obtener-publicaciones
  [qfn]
  (qfn :obtener-todo {:table "publicaciones"}))

(defn obtener-publicacion-por-id
  [qfn id]
  (qfn :obtener-por-id {:table "publicaciones"
                        :id id}))

(defn- actualizar-publicacion
  [qfn campo valor id]
  (let [campo (if-not (keyword? campo) (keyword campo) campo)]
    (qfn :actualizar-registro! {:table "publicaciones"
                                :updates {campo valor}
                                :id id})))
;;¿La necesito? 
#_(defn borrar-publicacion
  [qfn id]
  (qfn :borrar-por-id! {:table "publicaciones"
                        :id id}))

;;;;;;;;;;;;;;;;;;;;;;;;; REFERENCIAS ;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-referencia
  "qfn => Query function \n
   tipo_publicacion,titulo,ciudad,ano,editorial,ciudad, volumen, nombre_revista, nombre_libro => string \n
   autores => [uuid?]"
  [qfn tipo_publicacion titulo ano editorial ciudad volumen nombre_revista nombre_libro autores]
  (if (= *db-type* "xtdb")
    (datalog.schema/crear-doc-referencia! tipo_publicacion titulo ano editorial ciudad volumen nombre_revista nombre_libro autores)
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
  [qfn]
  (obtener-entidades qfn "referencias" :referencia/titulo)) 

(defn obtener-referencia-por-id 
  [qfn id]
  (obtener-entidad-por-id qfn "referencias" id))

(defn actualizar-referencia
  [qfn campo valor id]
  (let [campo (if-not (keyword? campo) (keyword campo) campo)
        actualiza-ref-sql (fn [] (qfn :actualizar-registro! {:table "referencias"
                                                             :updates {campo valor}
                                                             :id id}))]
    (cond
      (= *db-type* "xtdb") (datalog.queries/actualizar-entidad datalog.queries/node id campo valor)
      (= campo :autor) (->> (actualiza-ref-sql)
                            (actualizar-publicacion qfn campo valor))
      :else (actualiza-ref-sql))))

(defn borrar-referencia 
  [qfn id]
  (borrar-entidad qfn "referencias" id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;; CITAS ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-cita
  "qfn => Query function \n
   referencia => uuid \n
   cita, paginas => string \n
   usuario => uuid"
  [qfn referencia cita paginas usuario]
  (if (= *db-type* "xtdb")
    (datalog.schema/crear-doc-citas! referencia cita paginas usuario)
    (qfn :crear-cita! {:citas/referencia referencia
                       :citas/cita cita
                       :citas/paginas paginas
                       :citas/usuario usuario})))

(defn obtener-citas 
  [qfn]
  (obtener-entidades qfn "citas" :cita/referencia))

(defn obtener-cita-por-id 
  [qfn id]
  (obtener-entidad-por-id qfn "citas" id)) 

(defn actualizar-cita 
  [qfn campo valor id]
  (actualizar-entidad qfn "citas" campo valor id)) 

(defn borrar-cita 
  [qfn id]
  (borrar-entidad qfn "citas" id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;; COMENTARIOS ;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-comentario
  "qfn => Query function \n
   comentario, paginas, palabras_clave => string \n
   referencia, usuario => uuid"
  [qfn comentario paginas palabras_clave referencia usuario]
  (if (= *db-type* "xtdb")
    (datalog.schema/crear-doc-comentarios! referencia comentario paginas palabras_clave usuario)
    (qfn :crear-comentario! {:comentarios/referencia referencia
                             :comentarios/comentario comentario
                             :comentarios/paginas paginas
                             :comentarios/palabras_clave palabras_clave
                             :comentarios/usuario usuario})))

(defn obtener-comentarios 
  [qfn]
  (obtener-entidades qfn "comentarios" :comentario/referencia)) 

(defn obtener-comentario-por-id
  [qfn id]
  (obtener-entidad-por-id qfn "comentarios" id))

(defn actualizar-comentario 
  [qfn campo valor id]
  (actualizar-entidad qfn "comentarios" campo valor id)) 

(defn borrar-comentario 
  [qfn id]
  (borrar-entidad qfn "comentarios" id)) 

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; COLECCION-ITEMS ::::::::::::::::::::::::::

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; COLECCIONES ;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-coleccion
  "qfn => Query function \n
   nombre_coll => string \n
   referencias => uuid"
  [qfn nombre_coll & referencias]
  (if (= *db-type* "xtdb")
    (datalog.schema/crear-doc-colecciones! nombre_coll referencias)
    (let [coll-id (-> (qfn :crear-coleccion! {:colecciones/nombre_coll nombre_coll})
                      first
                      :id)
          items-ids (into [] (for [referencia referencias] (-> (crear-coleccion-item qfn coll-id referencia)
                                                               first
                                                               :id)))]
      {:coleccion coll-id
       :items items-ids})))

(defn obtener-colecciones 
  [qfn]
  (obtener-entidades qfn "colecciones" :coleccion/nombre_coll)) 

(defn obtener-coleccion-por-id
  [qfn id]
  (obtener-entidad-por-id qfn "coleccciones" id)) 

(defn actualizar-coleccion 
  [qfn campo valor id]
  (actualizar-entidad qfn "colecciones" campo valor id))

(defn borrar-coleccion 
  [qfn id]
  (borrar-entidad qfn "colecciones" id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; BIBLIOTECA-ITEMS ;;;;;;;;;;;;;;;;;;;;;;;;

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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; BIBLIOTECAS ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-biblioteca
  "qfn => Query function \n
   usuario => uuid \n
   nombre_biblioteca => string \n
   colecciones => uuid"
  [qfn usuario nombre_biblioteca & colecciones]
  (if (= *db-type* "xtdb")
    (datalog.schema/crear-doc-bibliotecas! usuario nombre_biblioteca (vec colecciones))
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
  [qfn]
  (obtener-entidades qfn "bibliotecas" :coleccion/nombre_coll))

(defn obtener-biblioteca-por-id
  [qfn id]
  (obtener-entidad-por-id qfn "bibliotecas" id))

(defn actualizar-biblioteca
  [qfn campo valor id]
  (actualizar-entidad qfn "bibliotecas" campo valor id))

(defn borrar-biblioteca
  [qfn id]
  (borrar-entidad qfn "bibliotecas" id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;; OTRAS CONSULTAS ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(comment
  *db-type* 
  ;Falta probar por SQL
  ;Es necesario crear una funcion que gestione las bibliotecas y las colecciones, de modo que 
  ; abstraiga las discrepancias entre los esquemas de SQL y Datalog.
  ;Podríamos integrar las de items en colecciones y bibliotecas respectivamente, tratando el caso del
  ; SQL donde se crearía la coleccion y el item 
  (def q (:db.sql/query-fn state/system))
  (tap> (obtener-usuarios q))     
  (actualizar-usuario q :nombre "Lisando Machado" #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b")
  (actualizar-usuario q :nombre "Miguel Marin" #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b")
  (obtener-usuario-por-id q #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b")
  (obtener-usuario-por-id q #uuid "46b37e5c-5242-4ea5-a963-46b4ca7ccaf1")
  (borrar-usuario q #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b") 
  (crear-usuario q "José Marín" "marino@gmail.com" "el_marine" "232jk sds **")
  (obtener-autores q)
  (obtener-autor-por-id q #uuid "961469a7-ff35-47a7-b136-9a807a7666a9")
  (actualizar-autor q :nombres "Julian Amado" #uuid "961469a7-ff35-47a7-b136-9a807a7666a9")
  (borrar-autor q #uuid "961469a7-ff35-47a7-b136-9a807a7666a9")
  (crear-autor q "Julio César" "Fabregas")
  (obtener-referencias q)
  (obtener-referencia-por-id q #uuid "966e1a6a-b667-4fb2-bd8b-56372ff99809")
  (actualizar-referencia q :ano 2009 #uuid "966e1a6a-b667-4fb2-bd8b-56372ff99809")
  (borrar-referencia q #uuid "966e1a6a-b667-4fb2-bd8b-56372ff99809")
  (crear-referencia q "Libro" "Los años de Valcarce" "2009" "El Farolito" "Madrid" nil nil nil [#uuid "ec9d24d5-2ca1-45d3-b97b-32d79c6619ba"])
  (tap>
   (crear-coleccion q "Coleccion Animal" #uuid "dc79fd79-e872-448b-b1bf-4b0bd1c2f748")) 
   (crear-coleccion q "Coleccion horizontes" #uuid "dc79fd79-e872-448b-b1bf-4b0bd1c2f748" (java.util.UUID/randomUUID))
  (obtener-publicaciones q)
  (into [] (for [autor [#uuid "2317eb29-30c0-4c7a-9b90-99ccdad4b0f4" 
                          #uuid "97d8cfe9-f260-4657-b483-05ad52d6a5eb"
                          #uuid "9f0a9109-cb33-4765-93e7-87d0f92cd838"]]
             (-> (crear-publicacion q #uuid "abb6620f-3746-4680-9969-2f1b96fe16e5" autor)
                 first
                 :id)))
  (datalog.schema/crear-doc-bibliotecas! (java.util.UUID/randomUUID) "Biblioteca mayor" (java.util.UUID/randomUUID) (java.util.UUID/randomUUID))
  (datalog.schema/crear-doc-colecciones! "Coleccion coleccionable" (java.util.UUID/randomUUID))
  (xtdb.api/attribute-stats datalog.queries/node) 

  '(+ 3 4 344)
  (eval '(+ 3 4 344))
  )