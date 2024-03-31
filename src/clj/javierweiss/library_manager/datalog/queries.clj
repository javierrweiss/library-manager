(ns javierweiss.library-manager.datalog.queries
  (:require
    [clojure.spec.alpha :as spec]
    [integrant.core :as ig]
    [integrant.repl.state :as state]
    [javierweiss.library-manager.datalog.documents :as documents]
    [xtdb.api :as xt]))

;; Conexión ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod ig/init-key :db.xtdb
  [_ {:keys [node]}]
  node)

(defn stop-xtdb!
  [nodo] 
  (.close nodo))

(defmethod ig/halt-key! :db.xtdb
  [_ {:keys [node]}]
  (stop-xtdb! node))

;; Utils ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro query-entity
  [llave pull-exp lvar]
  `{:find [~pull-exp]
    :where [[~lvar ~llave]]})


(defn q
  [nodo query & args]
  (apply xt/q (xt/db nodo) query args))


;; Consultas/DML ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn agregar-documentos
  "Uses XTDB put transaction to add a vector of documents to a specified
  node"
  [nodo docs]
  {:pre [(spec/valid? (spec/coll-of map? :kind vector?) docs)]}
  (try
    (xt/submit-tx nodo
                  (vec (for [doc docs]
                         [::xt/put doc])))
    (xt/sync nodo)
    (catch AssertionError e (.getMessage e))
    (catch IllegalArgumentException e (.getMessage e))
    (catch Exception e (.getMessage e))))


(defn agregar-doc
  "Persiste un solo documento a la base de datos"
  [nodo doc]
  {:pre [(spec/valid? map? doc)]}
  (try
    (let [id (:xt/id doc)]
      (xt/submit-tx nodo [[::xt/put doc]])
      (xt/sync nodo)
      id) 
    (catch AssertionError e (.getMessage e))
    (catch IllegalArgumentException e (.getMessage e))
    (catch Exception e (.getMessage e))))


(defn obtener-por-id
  "Recupera una entidad por su uuid"
  [nodo ident]
  {:pre [(spec/valid? uuid? ident)]}
  (q nodo '{:find [(pull ?ent [*])]
            :in [ident]
            :where [[?ent :xt/id ident]]}
     ident))

(defn borrar-por-id
  "Elimina una entidad según si uuid"
  [nodo ident]
  {:pre [(spec/valid? uuid? ident)]}
  (xt/submit-tx nodo [[::xt/delete ident]])
  (xt/sync nodo))


(defn actualizar-entidad
  "Recibe como argumentos un nodo, un uuid, un campo y un valor y actualiza la entidad correspondiente"
  [nodo ident k v]
  {:pre [(spec/valid? uuid? ident)]}
  (let [db (xt/db nodo)
        ent (xt/entity db ident)]
    (xt/submit-tx nodo [[::xt/put (assoc ent k v)]])))


(defn obtener-todas-las-entidades
  "Muestra todas las entidades que coincidan con la llave"
  [nodo k]
  {:pre [(spec/valid? keyword? k)]}
  (q nodo (query-entity k '(pull ?entidad [*]) '?entidad)))


(defn obtener-todos-usuarios
  "Muestra todas las entidades del mapa de entidades de tipo usuario"
  [nodo] 
  (q nodo '{:find [(pull ?usuario [*])]
            :where [[?usuario :usuario/nombre]]}))

(defn obtener-usuario-por-cuenta
  "Recupera entidad(es) por campos seleccionados"
  [nodo cuenta clave]
  (q nodo '{:find [(pull ?ent [*])]
            :in [[cuenta clave]]
            :where [[?ent :usuario/cuenta cuenta]
                    [?ent :usuario/clave clave]]} 
     [cuenta clave]))

(defn obtener-usuario-por-cuenta-y-correo
  "Recupera entidad(es) por campos seleccionados"
  [nodo cuenta correo]
  (q nodo '{:find [(pull ?ent [*])]
            :in [[cuenta clave]]
            :where [[?ent :usuario/cuenta cuenta]
                    [?ent :usuario/correo correo]]}
     [cuenta correo]))
 
(defn obtener-todas-referencias
  "Muestra todas las entidades del mapa de entidades de tipo referencia"
  [nodo] 
  (q nodo '{:find [(pull ?referencia [*])]
            :where [[?referencia :referencia/titulo]]}))


(defn obtener-todos-autores
  "Muestra todas las entidades del mapa de entidades de tipo autor"
  [nodo] 
  (q nodo '{:find [(pull ?autor [*])]
            :where [[?autor :autor/nombres]]}))


(defn obtener-todas-bibliotecas
  "Muestra todas las entidades del mapa de entidades de tipo biblioteca"
  [nodo] 
  (q nodo '{:find [(pull ?bib [*])]
            :where [[?bib :biblioteca/nombre_biblioteca]]}))


(defn obtener-todas-colecciones
  "Muestra todas las entidades del mapa de entidades de tipo coleccion"
  [nodo] 
  (obtener-todas-las-entidades nodo :coleccion/nombre_coll))


(defn obtener-todas-citas
  "Muestra todas las entidades del mapa de entidades de tipo cita"
  [nodo] 
  (q nodo '{:find [(pull ?citas [*])]
            :where [[?citas :cita/cita]]}))


(defn obtener-todos-comentarios
  "Muestra todas las entidades del mapa de entidades de tipo referencia"
  [nodo] 
  (q nodo '{:find [(pull ?comentario [*])]
            :where [[?comentario :comentario/comentario]]}))


(comment   
  (def node (:db.xtdb/node state/system))  
  (def t (xt/submit-tx node 
                       [[::xt/put {:xt/id (java.util.UUID/randomUUID)
                                   :nombre "Julia Abrañam"
                                   :dni 22533334}]]))
  (keys t)
  (::tx-id t)
  (tap> (keys (xt/attribute-stats node)))
 (map? (actualizar-entidad node #uuid "4eab2845-d0dc-4b91-9273-7fc2a69a314c" :nombre "Lino Albornoz")) 
  (xt/status node) 
  (xt/status node)
  (stop-xtdb! node)
  (type (type (:db.xtdb/node state/system)))
  (xt/attribute-stats node)
  (keys (xt/attribute-stats node))
  (agregar-doc node (documents/crear-doc-usuario! "Marcos Marcano" "marcossrcano@gmail.com" "mardsdssdio" "fddfdsd 243"))
  (actualizar-entidad node 32 :nombre "Karina Mango")
  (documents/crear-doc-usuario!  "Mario" "darthvader2323" "mariobros@gmail.com" "6556as asd")
  (documents/crear-doc-usuario! "Martin Palermo" "martpaler" "martinpalermo@gmail.com" "Gooool!")
  (documents/crear-doc-referencia! "Libro" "Los origenes del totalitarismo" "2006" "Taurus" "Madrid")
  (documents/crear-doc-referencia! "Articulo" "Meaning and Understanding" "1986" "Black & Wiley" "London" "1" "History & Theory")
  (documents/crear-doc-referencia! "Articulo"
                                "Historia Magistra Vitae"
                                "2000"
                                "Suhrkamp"
                                "Frankfurt am Main"
                                "2"
                                "Historische Zeitung"
                                nil
                                "Reinhart Kosellec"
                                "Herbert Spencer")
  (documents/crear-doc-referencia! "Articulo"
                                "Historia Magistra Vitae"
                                "2000"
                                "Suhrkamp"
                                "Frankfurt am Main"
                                "2"
                                "Historische Zeitung"
                                nil
                                ["Reinhart Koselleck"
                                 "Herbert Spencer"])
  (documents/crear-doc-referencia! "Articulo"
                                "Historia Magistra Vitae"
                                "2000"
                                "Suhrkamp"
                                "Frankfurt am Main"
                                "2"
                                "Historische Zeitung"
                                nil
                                [#uuid "7dc7655a-c753-449d-9e47-404ccf9c4511"
                                 #uuid "7dc7655a-c753-449d-9e47-404ccf9c4511"])
  (documents/crear-doc-autor! "Julián Enrique" "Alvarez Martínez")
  (documents/crear-doc-autor! "Julián Enrique" 15852)
  (documents/crear-doc-citas! #uuid "7dc7655a-c753-449d-9e47-404ccf9c4511"
                           "Esta es una cita"
                           "12334343-232133213"
                           #uuid "91ffe5d3-d3a4-423e-b472-6fed882d2abb")
  (documents/crear-doc-citas! #uuid "7dc7655a-c753-449d-9e47-404ccf9c4511"
                           "Esta es una cita"
                           "123-213"
                           #uuid "91ffe5d3-d3a4-423e-b472-6fed882d2abb")
  (documents/crear-doc-comentarios!  #uuid "e4d8d740-3009-45d5-b522-31977fa3f410"
                                  "Esto es un comentario comentado"
                                  "323-232"
                                  "alfa, beta, zeta"
                                  #uuid "7dc7655a-c753-449d-9e47-404ccf9c4511")
  (documents/crear-doc-comentarios!  #uuid "e4d8d740-3009-45d5-b522-31977fa3f410"
                                  "Esto es un comentario comentado"
                                  "323-232232322"
                                  "alfa, beta, zeta"
                                  #uuid "7dc7655a-c753-449d-9e47-404ccf9c4511")
  (documents/crear-doc-colecciones! "Ciencias Sociales"
                                 (java.util.UUID/randomUUID)
                                 (java.util.UUID/randomUUID)
                                 (java.util.UUID/randomUUID)
                                 (java.util.UUID/randomUUID))
  (documents/crear-doc-colecciones! "Ciencias Naturales"
                                 (java.util.UUID/randomUUID))
  (documents/crear-doc-bibliotecas! (java.util.UUID/randomUUID) "Biblioteca mayor" (java.util.UUID/randomUUID) (java.util.UUID/randomUUID))


  (def docs [(documents/crear-doc-usuario! "Mario"
                                        "mariobros@gmail.com"
                                        "darthvader2323"
                                        "6556as asd")
             (documents/crear-doc-usuario! "Santiago Mariño"
                                        "libertadorDeOriente"
                                        "marinoheroe@gmail.com"
                                        "212 dqe2 3")
             (documents/crear-doc-autor! "Julián Enrique"
                                      "Alvarez Martínez")
             (documents/crear-doc-autor! "Lionel"
                                      "Escaloni")])
  (def docs2 [(documents/crear-doc-referencia! "Articulo"
                                            "Meaning and Understanding"
                                            "2000"
                                            "Paidos"
                                            "Barcelona"
                                            nil
                                            nil
                                            "Contexto e Intepretación"
                                            [#uuid "76fa1e29-e59d-46df-8225-56aec7a8c84c"])
              (documents/crear-doc-referencia! "Articulo"
                                            "Historia Magistra Vitae"
                                            "2000"
                                            "Suhrkamp"
                                            "Frankfurt am Main"
                                            "2"
                                            "Historische Zeitung"
                                            nil
                                            [#uuid "76fa1e29-e59d-46df-8225-56aec7a8c84c"
                                             #uuid "f1d711a8-7b57-45f0-a3a6-cfd98ce5ba14"])])
  (def docs3 [(documents/crear-doc-citas! #uuid "7dc7655a-c753-449d-9e47-404ccf9c4511"
                                       "Esta es una cita"
                                       "133-232"
                                       #uuid "91ffe5d3-d3a4-423e-b472-6fed882d2abb")
              (documents/crear-doc-citas! #uuid "7dc7655a-c753-449d-9e47-404ccf9c4511"
                                       "Esta es una cita"
                                       "123-213"
                                       #uuid "91ffe5d3-d3a4-423e-b472-6fed882d2abb")
              (documents/crear-doc-comentarios!  #uuid "e4d8d740-3009-45d5-b522-31977fa3f410"
                                              "Esto es un comentario comentado"
                                              "323-232"
                                              "alfa, beta, zeta"
                                              #uuid "7dc7655a-c753-449d-9e47-404ccf9c4511")
              (documents/crear-doc-comentarios!  #uuid "e4d8d740-3009-45d5-b522-31977fa3f410"
                                              "Esto es un comentario comentado"
                                              "323-400"
                                              "alfa, beta, zeta"
                                              #uuid "7dc7655a-c753-449d-9e47-404ccf9c4511")])
  (def docs4 [(documents/crear-doc-autor! "Martin" "Martillo")
              (documents/crear-doc-usuario! "Hilario Cardozo" "hilarionc@gmail.com" "hilarito" "sdd werowe r")])
  (agregar-documentos node docs)
  (agregar-documentos node docs2)
  (agregar-documentos node docs3)
  (agregar-documentos node docs4)
  (agregar-documentos node {:a {:a 1 :b 2}
                            :b {:c 2 :d 343}
                            :f {:g 32 :h 3434}})
  (agregar-documentos 'nodito docs4)
  (agregar-documentos nil docs4)
  (agregar-doc node (documents/crear-doc-usuario! "Martin Palermo" "martinpalermo@gmail.com" "martpaler" "Gooool!"))
  
  (defmacro query-entity
    [llave pull-exp lvar] 
    `{:find [~pull-exp]
      :where [[~lvar ~llave]]}) 
   
  (q node (query-entity :usuario/nombre '(pull ?entidad [*]) '?entidad))
     
  (q node '{:find [(pull ?entidad [*])] 
            :where [[?entidad :usuario/nombre usuario]]
            :in [usuario]}
     "Hilario Cardozo")
  
  (obtener-todos-usuarios node)

  (obtener-usuario-por-cuenta node "juanmago" "98343hdjnsdds")

  
  (q node '{:find [(pull ?entidad [*])]
            :where [[?entidad :usuario/nombre "Lionel Messi"]]})
  
  (q node '{:find [{:user nombre :account cuenta}]
            :where [[e :usuario/nombre nombre]
                    [e :usuario/cuenta cuenta]]})

  (q node '{:find [(str name " " lastname)]
            :where [[e :autor/nombres name]
                    [e :autor/apellidos lastname]]})

  (q node '{:find [?id]
            :where [[e :xt/id ?id]
                    [e :usuario/correo "darthvader2323"]]})

  (q node '{:find [usuarios]
            :where [[_ :usuario/nombre usuarios]]})

  (q node '{:find [n]
            :where [[e :xt/id n]
                    [e :usuario/nombre "Marco Antonio"]]})

  (q node '{:find [n]
            :where [[e :xt/id n] [e :autor/apellidos "Escaloni"]]})

  (q node '{:find [(pull ?user [:usuario/nombre :usuario/cuenta :usuario/correo :usuarios/clave])]
            :where [[?user :xt/id ?uid]]})

  (q node '{:find [(pull ?user [{:keys [nombre cuenta clave]}])]
            :where [[?user :xt/id ?uid]]})

  (q node '{:find [?e ?n]
            :where [[?e :usuario/nombre ?n]
                    [?e :usuario/cuenta "darthvader2323"]]})

  (q node '{:find [(pull ?autor [:referencia/titulo
                                 :referencia/ano
                                 :referencia/volumen
                                 :referencia/ciudad
                                 {:referencia/autores [:autor/apellidos]}])]
            :where [[e :referencia/nombre_revista "Historische Zeitung"]
                    [e :xt/id ?autor]]})

  (q node '{:find [(pull ?autor [:referencia/titulo
                                 :referencia/ano
                                 :referencia/volumen
                                 :referencia/ciudad
                                 {:referencia/autores [:autor/apellidos :autor/nombres]}])]
            :where [[e :referencia/nombre_revista "Historische Zeitung"]
                    [e :xt/id ?autor]]})

  (q node '{:find [(pull ?ref [:referencia/titulo
                               :referencia/ano
                               :referencia/volumen
                               :referencia/ciudad])]
            :where [[e :xt/id ?ref]]})

  (q node '{:find [(pull ?entidad [*])]
            :where [[e :xt/id ?entidad]]})

  (obtener-todas-las-entidades node :autor/nombres)

  (tap> (obtener-todos-usuarios node))

  (tap> (obtener-todas-referencias node))

  (obtener-todos-autores node)

  (obtener-todas-bibliotecas node)

  (obtener-todos-comentarios node)

  (obtener-todas-colecciones node)

  (q node '{:find [(pull ?ref [*])] 
            :where [[?ref :referencia/tipo_publicacion]]})

  (def res (obtener-por-id node #uuid "4eab2845-d0dc-4b91-9273-7fc2a69a314c"))

  (ffirst res)

  (borrar-por-id node #uuid "4eab2845-d0dc-4b91-9273-7fc2a69a314c")
  
  (xt/entity (xt/db node) #uuid "76fa1e29-e59d-46df-8225-56aec7a8c84c")

  (actualizar-entidad node #uuid "76fa1e29-e59d-46df-8225-56aec7a8c84c" :autor/apellidos "Galindo")

  (xt/submit-tx node [[::xt/put
                       {:xt/id (java.util.UUID/randomUUID)
                        :usuario/nombre "Marco Antonio"
                        :usuario/correo "esddss@gmail.com"
                        :usuario/cuenta  "Eoo"
                        :usuario/clave "vamos sdcampioooon!!!"}]])
 
  (agregar-doc node (documents/crear-doc-colecciones! "Ciencias Naturales"
                                                 (java.util.UUID/randomUUID)))

  (obtener-todas-las-entidades node :coleccion/nombre_coll)
  (obtener-todas-las-entidades node :usuario/nombre)
  )
