(ns javierweiss.library-manager.web.controllers.usuario
  (:require
    [clojure.tools.logging :as log]
    [javierweiss.library-manager.db.db :as db]
    [javierweiss.library-manager.web.middleware.exception :as exception]
    [javierweiss.library-manager.web.routes.utils :as utils]
    [ring.util.http-response :as http-response]
    [hato.client :as hc]))

(defn crear-usuario
  [{{:keys [usuario_nombre usuario_cuenta usuario_correo usuario_clave]} :params :as req}] 
    (log/debug "Estos son los body-params " (:body-params req)
               "Estos son los form-params " (:form-params req)
               "Estos son los params " (:params req)
               "Este es el request " req)
  (let [{:keys [query-fn]} (utils/route-data req)]
    (try
      (log/debug "Creando usuario con los parámetros " usuario_nombre usuario_cuenta usuario_correo)
      (if (some nil? [usuario_nombre usuario_cuenta usuario_correo usuario_clave])
        (http-response/bad-request {:body "Ningún campo debe quedar vacío"})
        (do (db/crear-usuario query-fn usuario_nombre usuario_cuenta usuario_correo usuario_clave)
            (http-response/ok {:body "Registro enviado con éxito"})))
      (catch Exception e
        (log/error "Error al crear usuario")
        (exception/handler (.getMessage e) 0 e req)))))


(defn actualizar-usuario
  [])


(defn borrar-usuario
  [])


(comment 
   
  (require '[hato.client :as hc])
  (def c (hc/build-http-client {:connect-timeout 10000
                                :redirect-policy :always
                               :ssl-context {:insecure? true}}))
  (hc/post "http://127.0.0.1:3000/api/usuario" 
           {:form-params {:nombre "Fulano" :cuenta "fulano233" :correo "fulano@gmail.com" :clave "332ssd··"}  
            :content-type :json} 
            {:http-client c})
  (hc/post "http://127.0.0.1:3000/api/usuario"
           {:form-params {:nombre "Fulano" :cuenta "fulano233" :correo "fulano@gmail.com" :clave 1546465}
            :content-type :json}
           {:http-client c})
  

  :ex 
  (crear-usuario (hc/post "http://127.0.0.1:3000/api/usuario"
                          {:form-params {:nombre "Fulano" :cuenta "fulano233" :correo "fulano@gmail.com" :clave "332ssd··"}
                           :content-type :json}
                          {:http-client c}))
  :ex

)