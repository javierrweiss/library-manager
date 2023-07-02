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
    (log/debug "Estos son los body-params " (:body-params req)
               "Estos son los form-params " (:form-params req)
               "Estos son los params " (:params req))
  (binding [db/*db-type* (:db (utils/route-data req))]
    (log/debug "El tipo de base de datos es: " db/*db-type*)
    (let [{:keys [query-fn]} (utils/route-data req)]
      (try
        (log/debug "Creando usuario con los parámetros " usuario_nombre usuario_cuenta usuario_correo)
        (if (some nil? [usuario_nombre usuario_cuenta usuario_correo usuario_clave])
          (http-response/bad-request "Ningún campo debe quedar vacío")
          (do (db/crear-usuario query-fn usuario_nombre usuario_correo usuario_cuenta usuario_clave)
              (http-response/ok "Registro enviado con éxito")))
        (catch Exception e
          (log/error "Error al crear usuario")
          (exception/handler (.getMessage e) 0 e req))))))


(defn actualizar-nombre-usuario
  [{{:keys [usuario_nombre]} :params
    {:keys [id]} :query-params :as req}]
  (log/debug "Estos son los body-params " (:body-params req)
             "Estos son los form-params " (:form-params req)
             "Estos son los params " (:params req))
  (let [{:keys [query-fn]} (utils/route-data req)]
    (try
      (db/actualizar-usuario query-fn 'usuario usuario_nombre id)
      (http-response/ok "Nombre de usuario cambiado con éxito")
      (catch Exception e
           (log/error "Error al actualizar usuario")
           (exception/handler (.getMessage e) 0 e req)))))

(defn actualizar-correo-usuario
  [{{:keys [usuario_correo]} :params
    {:keys [id]} :query-params :as req}]
  (log/debug "Estos son los body-params " (:body-params req)
             "Estos son los form-params " (:form-params req)
             "Estos son los params " (:params req))
  (let [{:keys [query-fn]} (utils/route-data req)]
    (try
      (db/actualizar-usuario query-fn 'correo usuario_correo id)
      (http-response/ok "Correo actualizado con éxito")
      (catch Exception e
        (log/error "Error al actualizar usuario")
        (exception/handler (.getMessage e) 0 e req)))))

(defn actualizar-clave-usuario
  [{{:keys [usuario_clave]} :params 
    {:keys [id]} :query-params :as req}]
  (log/debug "Estos son los body-params " (:body-params req)
             "Estos son los form-params " (:form-params req)
             "Estos son los params " (:params req))
  (let [{:keys [query-fn]} (utils/route-data req)]
    (try
      (db/actualizar-usuario query-fn 'clave usuario_clave id)
      (http-response/ok "Clave actualizada con éxito")
      (catch Exception e
        (log/error "Error al actualizar usuario")
        (exception/handler (.getMessage e) 0 e req)))))

(defn borrar-usuario
  [{{:keys [id]} :query-params :as req}]
  (log/debug "Estos son los body-params " (:body-params req)
             "Estos son los form-params " (:form-params req)
             "Estos son los params " (:params req))
  (let [{:keys [query-fn]} (utils/route-data req)]
    (try
      (db/borrar-usuario query-fn id)
      (http-response/ok "Usuario borrado")
      (catch Exception e
        (log/error "Error al eliminar usuario")
        (exception/handler (.getMessage e) 0 e req)))))

(defn obtener-usuario
  [{{:keys [id]} :query-params :as req}]
  (log/debug "Estos son los body-params " (:body-params req)
             "Estos son los form-params " (:form-params req)
             "Estos son los params " (:params req))
  (let [{:keys [query-fn]} (utils/route-data req)]
    (try
      (db/obtener-usuario-por-id query-fn id)
      (http-response/ok "Usuario obtenido")
      (catch Exception e
        (log/error "Error al recuperar usuario")
        (exception/handler (.getMessage e) 0 e req)))))


(comment

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

  db/*db-type*
  (:db/type state/system)

  (db/crear-usuario (:db/conn state/system) "Fulano" "fulano233" "fulano@gmail.com" "332ssd··")
   
  (db/obtener-usuarios (:db/conn state/system))

  (def req {:params {:usuario_nombre "Fulano"}
            :query-params {:id 123456}})
  
  (defn dummy-fn
    [{{:keys [usuario_nombre]} :params
      {:keys [id]} :query-params :as re}]
    re)
  
  (dummy-fn req)
   
  )