(ns javierweiss.library-manager.web.controllers.usuario
  (:require
   [clojure.tools.logging :as log]
   [javierweiss.library-manager.db.db :as db]
   [javierweiss.library-manager.web.middleware.exception :as exception]
   [javierweiss.library-manager.web.routes.utils :as utils]
   [ring.util.http-response :as http-response]
   [javierweiss.library-manager.web.views.usuario :as u]
   [javierweiss.library-manager.web.htmx :as htmx]
   [clojure.string :as string]
   [integrant.repl.state :as state]))

(defn crear-usuario
  [{{:keys [registro_usuario_nombre registro_usuario_cuenta registro_usuario_correo registro_usuario_clave]} :params :as req}] 
  (log/debug "Estos son los params " (:params req)) 
  (let [q (utils/route-data req)]
    (try
      (log/debug "Creando usuario con los parámetros " registro_usuario_nombre registro_usuario_cuenta registro_usuario_correo)
      (if (some string/blank? [registro_usuario_nombre registro_usuario_cuenta registro_usuario_correo registro_usuario_clave])
         (-> (http-response/bad-request (htmx/page-htmx (u/registro_no_exitoso req "Debe completar todos los campos"))) :body) ;; Delegar validación al formulario
        (let [id (-> (db/crear-usuario q registro_usuario_nombre registro_usuario_correo registro_usuario_cuenta registro_usuario_clave)
                     first 
                     :id)]
            (-> (http-response/created (str "/usuario/" id) (htmx/page-htmx (u/registro_exitoso req))) :body)))
      (catch Exception e 
        (exception/handler "Error al crear usuario" 500 e req)))))

(defn actualizar-nombre-usuario
  [{{:keys [id usuario_nombre]} :params :as req}] 
  (log/debug "Estos son los params " (:params req))
  (let [q (utils/route-data req)]
    (try
      (db/actualizar-usuario q :nombre usuario_nombre (java.util.UUID/fromString id))
      (http-response/ok "Nombre de usuario cambiado con éxito")
      (catch Exception e 
           (exception/handler "Error al actualizar usuario"  500 e req)))))

(defn actualizar-correo-usuario
  [{{:keys [id usuario_correo]} :params :as req}] 
  (log/debug "Estos son los params " (:params req))
  (let [q (utils/route-data req)]
    (try
      (db/actualizar-usuario q :correo usuario_correo (java.util.UUID/fromString id))
      (http-response/ok "Correo actualizado con éxito")
      (catch Exception e 
        (exception/handler "Error al actualizar usuario" 500 e req)))))

(defn actualizar-clave-usuario
  [{{:keys [id usuario_clave]} :params :as req}] 
  (log/debug "Estos son los params " (:params req))
  (let [q (utils/route-data req)]
    (try
      (db/actualizar-usuario q :clave usuario_clave (java.util.UUID/fromString id))
      (http-response/ok "Clave actualizada con éxito")
      (catch Exception e 
        (exception/handler "Error al actualizar usuario" 500 e req)))))

(defn borrar-usuario
  [{{:keys [id]} :params :as req}] 
  (log/debug "Estos son los params " (:params req))
  (let [q (utils/route-data req)]
    (try
      (db/borrar-usuario q (java.util.UUID/fromString id))
      (http-response/ok "Usuario borrado")
      (catch Exception e 
        (exception/handler "Error al eliminar usuario" 500 e req)))))

(defn obtener-usuario
  [{{:keys [id]} :params :as req}] 
  (log/debug "Estos son los params " (:params req))
  (let [q (utils/route-data req)] 
    (try
      (-> (db/obtener-usuario-por-id q (java.util.UUID/fromString id))
          (http-response/ok))
      (catch Exception e 
        (exception/handler "Error al recuperar usuario" 500 e req)))))

(defn obtener-todos-usuarios
  [req] 
  (try
    (let [q (utils/route-data req)]
      (-> (db/obtener-usuarios q)  
          (http-response/ok)))
    (catch Exception e 
      (exception/handler "Error al obtener usuarios" 500 e req))))

 
(comment  
  
  :dbg   
  
  (http-response/bad-request (htmx/page-htmx (u/registro_exitoso {:status 200 :headers "" :body ""})))

  (db/crear-usuario (second ((:reitit.routes/api state/system))) "Juan Mora" "juanmora@gmail.com" "juanmora" "dssdr333")
  
  )