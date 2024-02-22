(ns javierweiss.library-manager.web.controllers.usuario
  (:require
    [clojure.tools.logging :as log]
    [javierweiss.library-manager.db.db :as db]
    [javierweiss.library-manager.web.middleware.exception :as exception]
    [javierweiss.library-manager.web.routes.utils :as utils]
    [ring.util.http-response :as http-response]
    [hato.client :as hc]
    [integrant.repl.state :as state]))

(defn crear-usuario
  [{{:keys [usuario_nombre usuario_cuenta usuario_correo usuario_clave]} :params :as req}] 
  (tap> req)
  (log/debug "Estos son los params " (:params req)) 
  (let [q (utils/route-data req)]
    (try
      (log/debug "Creando usuario con los parámetros " usuario_nombre usuario_cuenta usuario_correo)
      (if (some nil? [usuario_nombre usuario_cuenta usuario_correo usuario_clave])
        (http-response/bad-request "Ningún campo debe quedar vacío")
        (do (db/crear-usuario q usuario_nombre usuario_correo usuario_cuenta usuario_clave)
            (http-response/created "Registro creado con éxito")))
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
  (tap> req)
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
  (tap> req) 
  (try
    (let [q (utils/route-data req)]
      (-> (db/obtener-usuarios q)  
          (http-response/ok)))
    (catch Exception e 
      (exception/handler "Error al obtener usuarios" 500 e req))))

 
(comment  
  (def cc (->  state/system :reitit.routes/api))
  
   (->
   (db/obtener-usuario-por-id (utils/route-data cc) (java.util.UUID/fromString "46b37e5c-5242-4ea5-a963-46b4ca7ccaf1"))
   (http-response/ok))

  (def c (hc/build-http-client {:connect-timeout 10000
                                :redirect-policy :always
                                :ssl-context {:insecure? true}}))

  (tap> @(hc/get "http://127.0.0.1:3000/api/v1/usuario/todos" ))
 
  (tap> (hc/delete "http://127.0.0.1:3000/api/usuario/del" {:query-params {:id "17ac5d64-c0d8-4a48-bb83-2dff987af89d"}}))

  (hc/post "http://127.0.0.1:3000/api/usuario"
           {:params {:usuario_nombre "Fulano" :usuario_cuenta "fulano233" :usuario_correo "fulano@gmail.com" :usuario_clave "332ssd··"}
            :content-type :json}
           {:http-client c})
  (hc/post "http://127.0.0.1:3000/api/usuario"
           {:params {:usuario_nombre "Fulano" :usuario_cuenta "fulano233" :usuario_correo "fulano@gmail.com" :usuario_clave 1546465}
            :content-type :json}
           {:http-client c})
     
  :dbg   
  :ex
  (crear-usuario (hc/post "http://127.0.0.1:3000/api/usuario"
                          {:form-params {:nombre "Fulano" :cuenta "fulano233" :correo "fulano@gmail.com" :clave "332ssd··"}
                           :content-type :json}
                          {:http-client c}))
  :ex
  

  (hc/get "http://127.0.0.1:3000/api/usuario"
          {:query-params {:id "46b37e5c-5242-4ea5-a963-46b4ca7ccaf1"}
           #_:content-type #_:json}
          {:http-client c})
  :ex
  (java.util.UUID/fromString  "46b37e5c-5242-4ea5-a963-46b4ca7ccaf1")
  )