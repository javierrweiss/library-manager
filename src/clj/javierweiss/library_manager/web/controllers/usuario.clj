(ns javierweiss.library-manager.web.controllers.usuario
  (:require
   [clojure.tools.logging :as log]
   [javierweiss.library-manager.db.db :as db]
   [javierweiss.library-manager.web.middleware.exception :as exception]
   [javierweiss.library-manager.web.views.errores :as err]
   [javierweiss.library-manager.web.views.exito :refer [exito]]
   [javierweiss.library-manager.web.routes.utils :as utils]
   [javierweiss.library-manager.web.security.loginsystem :refer [logear-usuario]]
   [ring.util.http-response :as http-response]
   [javierweiss.library-manager.web.views.usuario :as u]
   [javierweiss.library-manager.web.htmx :as htmx]
   [clojure.string :as string]
   [integrant.repl.state :as state] 
   [xtdb.api :as xtdb]))

(defn crear-usuario
  [{{:keys [registro_usuario_nombre registro_usuario_cuenta registro_usuario_correo registro_usuario_clave]} :params :as req}] 
  (log/debug "Estos son los params " (:params req)) 
  (let [q (utils/route-data req)
        clave (.getBytes registro_usuario_clave)]
    (try
      (log/debug "Creando usuario con los parámetros " registro_usuario_nombre registro_usuario_cuenta registro_usuario_correo)
      (if (some string/blank? [registro_usuario_nombre registro_usuario_cuenta registro_usuario_correo registro_usuario_clave])
         (-> (http-response/bad-request (htmx/page-htmx (u/registro_no_exitoso "Debe completar todos los campos"))) :body) ;; Delegar validación al formulario
        (let [resp (db/crear-usuario q registro_usuario_nombre registro_usuario_correo registro_usuario_cuenta clave)
              id (if-not (uuid? resp)
                   (-> resp
                       first
                       :id)
                   resp)]
            (-> (http-response/created (str "/usuario/" id) (htmx/page-htmx (exito req "Usuario creado"))) :body)))
      (catch Exception e 
        (exception/handler (:body (htmx/page-htmx (err/error req "¡No se pudo crear el usuario!"))) "Error al crear usuario" 500 e req)))))

(defn actualizar-nombre-usuario
  [{{:keys [id usuario_nombre]} :params :as req}] 
  (log/debug "Estos son los params " (:params req))
  (let [q (utils/route-data req)]
    (try
      (db/actualizar-usuario q :nombre usuario_nombre (java.util.UUID/fromString id))
      (-> (http-response/ok (htmx/page-htmx (exito req "Usuario actualizado"))) :body)
      (catch Exception e 
           (exception/handler (:body (htmx/page-htmx (err/error req (str "No se pudo actualizar el campo <<nombre>>")))) "Error al actualizar usuario" 500 e req)))))

(defn actualizar-correo-usuario
  [{{:keys [id usuario_correo]} :params :as req}] 
  (log/debug "Estos son los params " (:params req))
  (let [q (utils/route-data req)]
    (try
      (db/actualizar-usuario q :correo usuario_correo (java.util.UUID/fromString id))
      (-> (http-response/ok (htmx/page-htmx (exito req "Usuario actualizado"))) :body)
      (catch Exception e 
        (exception/handler (:body (htmx/page-htmx (err/error req (str "No se pudo actualizar el campo <<correo>>")))) "Error al actualizar usuario" 500 e req)))))

(defn actualizar-clave-usuario
  [{{:keys [id usuario_clave]} :params :as req}] 
  (log/debug "Estos son los params " (:params req))
  (let [q (utils/route-data req)]
    (try
      (db/actualizar-usuario q :clave usuario_clave (java.util.UUID/fromString id))
      (-> (http-response/ok (htmx/page-htmx (exito req "Usuario actualizado"))) :body)
      (catch Exception e 
        (exception/handler (:body (htmx/page-htmx (err/error req (str "No se pudo actualizar el campo <<clave>>")))) "Error al actualizar usuario" 500 e req)))))

(defn borrar-usuario
  [{{:keys [id]} :params :as req}] 
  (log/debug "Estos son los params " (:params req))
  (let [q (utils/route-data req)]
    (try
      (db/borrar-usuario q (java.util.UUID/fromString id))
      (-> (http-response/ok (htmx/page-htmx (exito req "Usuario eliminado"))) :body)
      (catch Exception e 
        (exception/handler (:body (htmx/page-htmx (err/error req (str "No se pudo eliminar el usuario")))) "Error al eliminar usuario" 500 e req)))))

(defn obtener-usuario-por-id 
  [{{:keys [id]} :params :as req}] 
  (log/debug "Estos son los params " (:params req))
  (let [q (utils/route-data req)] 
    (try
      (-> (db/obtener-usuario-por-id q (java.util.UUID/fromString id))
          (htmx/page-htmx (u/muestra_usuario))
          (http-response/ok)
          :body)
      (catch Exception e 
        (exception/handler (:body (htmx/page-htmx (err/error req (str "No se pudo recuperar la información del usuario")))) "Error al recuperar usuario" 500 e req)))))

(defn obtener-usuario
  [{{:keys [cuenta correo contrasena]} :params :as req}]
  (log/debug "Estos son los params " (:params req))
  (let [q (utils/route-data req)
        {:keys [id clave] :as usr} (some-> (db/obtener-usuario q cuenta correo) utils/traverse-db-result utils/obtener-datos-usuario)]
    (try
      (if usr
        (let [resp (logear-usuario id clave contrasena)]
          (if (bytes? resp)
            (-> (assoc req :user-credentials resp)
                (htmx/page-htmx (u/muestra_usuario))
                :body)
            resp))
        (:body (htmx/page-htmx (err/usuario_inexistente req))))
      (catch Exception e
        (exception/handler (:body (htmx/page-htmx (err/login_unsuccessful req))) "Login no exitoso" 403 e req)))))

(defn obtener-todos-usuarios
  [req] 
  (try
    (let [q (utils/route-data req)]
      (-> (db/obtener-usuarios q)  
          (http-response/ok)))
    (catch Exception e 
      (exception/handler (:body (err/error req "No se pudieron cargar los usuarios")) "Error al obtener usuarios" 500 e req))))

 
(comment  
  
  :dbg   

  ;  :params {:login_ ["Bermejo" "javierweiss@gmail.com" "123456"]} Así llegan los params en el request
  
  (http-response/bad-request (htmx/page-htmx (u/registro_exitoso {:status 200 :headers "" :body ""})))

  (db/crear-usuario (second ((:reitit.routes/api state/system))) "Juan Mora" "juanmora@gmail.com" "juanmora" (.getBytes "dssdr333"))

  (def u (db/obtener-usuario (second ((:reitit.routes/api state/system))) "Admin" "javierweiss@gmail.com"))
 
  (seq (db/obtener-usuario (second ((:reitit.routes/api state/system))) "Admin" "kiow@gmail.com"))
  
  (defn traverse 
    [ds]
    (loop [ds (if (set? ds) (seq ds) ds)]
      (if (or (map? ds) (not (coll? ds)))
        ds
        (recur (first ds)))))
  
  (let [xtdb {:db-type :xtdb :query-fn (:db.xtdb/node state/system)}
        sql {:db-type :sql :query-fn (:db.sql/query-fn state/system)} 
        #_usr #_(utils/traverse-db-result (db/obtener-usuario xtdb "Admin" "javierweiss@gmail.com"))]
    (some-> (db/obtener-usuario sql "Bermejo" "javierweiss@gmail.com") utils/traverse-db-result utils/obtener-datos-usuario)
    #_(utils/obtener-datos-usuario usr)) 
  
  ;; Creo que fue una pésima idea lo de implementar soporte para ambas bases de datos...
  )