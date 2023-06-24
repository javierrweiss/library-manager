(ns javierweiss.library-manager.web.controllers.usuario
  (:require
    [clojure.tools.logging :as log]
    [javierweiss.library-manager.db.db :as db]
    [javierweiss.library-manager.web.middleware.exception :as exception]
    [javierweiss.library-manager.web.routes.utils :as utils]
    [ring.util.http-response :as http-response]))


(defn crear-usuario
  [{{:strs [nombre cuenta correo clave]} :form-params :as request}]
  (log/debug "Creando usuario con los parámetros " nombre cuenta correo)
  (let [{:keys [query-fn]} (utils/route-data request)]
    (try
      (db/crear-usuario query-fn nombre correo cuenta clave)
      (http-response/ok "Registro enviado con éxito")
      (catch Exception e
        (log/error "Error al crear usuario")
        (exception/handler (.getMessage e) 0 e request)))))


(defn actualizar-usuario
  [])


(defn borrar-usuario
  [])
