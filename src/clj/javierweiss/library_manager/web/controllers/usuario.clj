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
  (log/debug "Estos son los body-params " (:body-params req)
             "Estos son los form-params " (:form-params req)
             "Estos son los params " (:params req)) 
  (let [{:keys [conn type]} (first (:query-fn (utils/route-data req)))]
    (try
      (log/debug "Creando usuario con los parámetros " usuario_nombre usuario_cuenta usuario_correo)
      (if (some nil? [usuario_nombre usuario_cuenta usuario_correo usuario_clave])
        (http-response/bad-request "Ningún campo debe quedar vacío")
        (do (db/crear-usuario type conn usuario_nombre usuario_correo usuario_cuenta usuario_clave)
            (http-response/ok "Registro enviado con éxito")))
      (catch Exception e
        (log/error "Error al crear usuario")
        (exception/handler (.getMessage e) 0 e req)))))


(defn actualizar-nombre-usuario
  [{{:keys [usuario_nombre]} :params
    {:keys [id]} :query-params :as req}]
  (tap> req)
  (log/debug "Estos son los body-params " (:body-params req)
             "Estos son los form-params " (:form-params req)
             "Estos son los params " (:params req))
  (let [{:keys [conn type]} (first (:query-fn (utils/route-data req)))]
    (try
      (db/actualizar-usuario type conn 'usuario usuario_nombre (java.util.UUID/fromString id))
      (http-response/ok "Nombre de usuario cambiado con éxito")
      (catch Exception e
           (log/error "Error al actualizar usuario")
           (exception/handler (.getMessage e) 0 e req)))))

(defn actualizar-correo-usuario
  [{{:keys [usuario_correo]} :params
    {:keys [id]} :query-params :as req}]
  (tap> req)
  (log/debug "Estos son los body-params " (:body-params req)
             "Estos son los form-params " (:form-params req)
             "Estos son los params " (:params req))
  (let [{:keys [conn type]} (first (:query-fn (utils/route-data req)))]
    (try
      (db/actualizar-usuario type conn 'correo usuario_correo (java.util.UUID/fromString id))
      (http-response/ok "Correo actualizado con éxito")
      (catch Exception e
        (log/error "Error al actualizar usuario")
        (exception/handler (.getMessage e) 0 e req)))))

(defn actualizar-clave-usuario
  [{{:keys [usuario_clave]} :params 
    {:keys [id]} :query-params :as req}]
  (tap> req)
  (log/debug "Estos son los body-params " (:body-params req)
             "Estos son los form-params " (:form-params req)
             "Estos son los params " (:params req))
  (let [{:keys [conn type]} (first (:query-fn (utils/route-data req)))]
    (try
      (db/actualizar-usuario type conn 'clave usuario_clave (java.util.UUID/fromString id))
      (http-response/ok "Clave actualizada con éxito")
      (catch Exception e
        (log/error "Error al actualizar usuario")
        (exception/handler (.getMessage e) 0 e req)))))

(defn borrar-usuario
  [{{:keys [id]} :query-params :as req}]
  (tap> req)
  (log/debug "Estos son los body-params " (:body-params req)
             "Estos son los form-params " (:form-params req)
             "Estos son los params " (:params req))
  (let [{:keys [conn type]} (first (:query-fn (utils/route-data req)))]
    (try
      (db/borrar-usuario type conn (java.util.UUID/fromString id))
      (http-response/ok "Usuario borrado")
      (catch Exception e
        (log/error "Error al eliminar usuario")
        (exception/handler (.getMessage e) 0 e req)))))

(defn obtener-usuario
  [{{:keys [id]} :query-params :as req}]
  (tap> req)
  (log/debug "Estos son los body-params " (:body-params req)
             "\n Estos son los form-params " (:form-params req)
             "\n Estos son los params " (:params req)
             "\n Este es el valor de la llave query-fn " (:query-fn (utils/route-data req)))
  (let [{:keys [conn type]} (first (:query-fn (utils/route-data req)))]
    (log/debug "Conexión: " conn "\n Tipo DB: " type)
    (try
      (-> (db/obtener-usuario-por-id type conn (java.util.UUID/fromString id))
          (http-response/ok))
      (catch Exception e
        (log/error "Error al recuperar usuario")
        (exception/handler (.getMessage e) 0 e req)))))


(comment
  (def c (->  state/system :reitit.routes/api second :query-fn first :conn))
  (->
   (db/obtener-usuario-por-id "xtdb" c (java.util.UUID/fromString "46b37e5c-5242-4ea5-a963-46b4ca7ccaf1"))
   (http-response/ok))
  (require '[hato.client :as hc])
  (def c (hc/build-http-client {:connect-timeout 10000
                                :redirect-policy :always
                                :ssl-context {:insecure? true}}))
  (hc/post "http://127.0.0.1:3000/api/usuario"
           {:params {:usuario_nombre "Fulano" :usuario_cuenta "fulano233" :usuario_correo "fulano@gmail.com" :usuario_clave "332ssd··"}
            :content-type :json}
           {:http-client c})
  (hc/post "http://127.0.0.1:3000/api/usuario"
           {:params {:usuario_nombre "Fulano" :usuario_cuenta "fulano233" :usuario_correo "fulano@gmail.com" :usuario_clave 1546465}
            :content-type :json}
           {:http-client c})

  "curl -F usuario_nombre=Javier -F usuario_cuenta=javierzihno -F usuario_correo=correoescorreo -F usuario_clave=keywords22 http://localhost:3000/api/usuario"

  :ex
  (crear-usuario (hc/post "http://127.0.0.1:3000/api/usuario"
                          {:form-params {:nombre "Fulano" :cuenta "fulano233" :correo "fulano@gmail.com" :clave "332ssd··"}
                           :content-type :json}
                          {:http-client c}))
  :ex
  (require '[integrant.repl.state :as state])
  
  (hc/get "http://127.0.0.1:3000/api/usuario"
          {:query-params {:id "46b37e5c-5242-4ea5-a963-46b4ca7ccaf1"}
           #_:content-type #_:json}
          {:http-client c})
  :ex
  (java.util.UUID/fromString  "46b37e5c-5242-4ea5-a963-46b4ca7ccaf1")
  )