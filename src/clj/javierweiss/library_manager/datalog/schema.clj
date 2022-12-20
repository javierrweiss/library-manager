(ns javierweiss.library-manager.datalog.schema
  (:require [clojure.spec.alpha :as spec]))

;;;;;;;;;;;;;;; Specs ;;;;;;;;;;;;;;;;;;;;;

(spec/def :schema/pags (spec/and string? #(< (count %) 10)))
(spec/def :schema/ids (spec/or :a nil? :b (spec/coll-of uuid?)))
(spec/def :schema/correo (spec/and string? #(re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$" %)))

;;;;;;;;;;;;;;;;;;; Documentos/Schema ;;;;;;;;;;;;;;;;;;;;;;;;

(defn crear-doc-usuario!
  [nombre correo cuenta clave]
  {:pre [(spec/valid? (spec/* string?) [nombre cuenta clave])
         (spec/valid? :schema/correo correo)]}
  {:xt/id (java.util.UUID/randomUUID)
   :usuario/nombre nombre
   :usuario/correo correo
   :usuario/cuenta cuenta
   :usuario/clave clave})

(defn crear-doc-autor!
  [nombres apellidos]
  {:pre [(spec/valid?  (spec/* string?) [nombres apellidos])]}
  {:xt/id (java.util.UUID/randomUUID)
   :autor/nombres nombres
   :autor/apellidos apellidos})

(defn crear-doc-referencia!
  [tipo_publicacion titulo ano editorial ciudad & [volumen nombre_revista nombre_libro autores]]
  {:pre [(spec/valid? (spec/* string?) [tipo_publicacion titulo ano editorial ciudad])
         (spec/valid? (spec/* (spec/or :a string? :b nil?)) [volumen nombre_revista nombre_libro])
         (spec/valid? :schema/ids autores)]}
  {:xt/id (java.util.UUID/randomUUID)
   :referencia/tipo_publicacion tipo_publicacion
   :referencia/titulo titulo
   :referencia/ano ano
   :referencia/editorial editorial
   :referencia/ciudad ciudad
   :referencia/volumen volumen
   :referencia/nombre_revista nombre_revista
   :referencia/nombre_libro nombre_libro
   :referencia/autores autores})

(defn crear-doc-citas!
  [referencia cita paginas usuario]
  {:pre [(spec/valid? string? cita)
         (spec/valid? :schema/ids [referencia usuario])
         (spec/valid? :schema/pags paginas)]}
  {:xt/id (java.util.UUID/randomUUID)
   :cita/referencia referencia
   :cita/cita cita
   :cita/paginas paginas
   :cita/usuario usuario})

(defn crear-doc-comentarios!
  [referencia comentario paginas palabras_clave usuario]
  {:pre [(spec/valid? :schema/ids [referencia usuario])
         (spec/valid? :schema/pags paginas)
         (spec/valid? (spec/* string?) [palabras_clave comentario])]}
  {:xt/id (java.util.UUID/randomUUID)
   :comentario/referencia referencia
   :comentario/comentario comentario
   :comentario/paginas paginas
   :comentario/palabras_clave palabras_clave
   :comentario/usuario usuario})

(defn crear-doc-bibliotecas!
  [usuario nombre_biblioteca & colecciones]
  {:pre [(spec/valid? string? nombre_biblioteca)
         (spec/valid? uuid? usuario)
         (spec/valid? :schema/ids colecciones)]}
  {:xt/id (java.util.UUID/randomUUID)
   :biblioteca/usuario usuario
   :biblioteca/nombre_biblioteca nombre_biblioteca
   :biblioteca/colecciones (vec colecciones)})


(defn crear-doc-colecciones!
  [nombre_coll & referencias]
  {:pre [(spec/valid? string? nombre_coll)
         (spec/valid? (spec/or :a nil? :b (spec/coll-of uuid?)) referencias)]}
  {:xt/id (java.util.UUID/randomUUID)
   :coleccion/nombre_coll nombre_coll
   :coleccion/referencias (vec referencias)})

