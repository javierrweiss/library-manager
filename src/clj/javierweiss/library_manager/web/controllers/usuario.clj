(ns javierweiss.library-manager.web.controllers.usuario
  (:require [javierweiss.library-manager.db.db :as db]
            [clojure.tools.logging :as log]
            [ring.util.http-response :as http-response]
            [javierweiss.library-manager.web.routes.utils :as utils]))

(defn crear-usuario
  [{{:strs [nombre cuenta correo clave]} :form-params :as request}]
  (log/debug "Creando usuario con los parámetros " nombre cuenta correo)
  (let [{:keys [query-fn]} (utils/route-data request)]
    (try 
      (db/crear-usuario query-fn nombre correo cuenta clave)
      (http-response/found "/")
      (catch Exception e
        (log/error "Error al crear usuario")
        (-> (http-response/found "/")
            (assoc :flash {:errors {:unknown (.getMessage e)}}))))))

(defn actualizar-usuario 
  [])

(defn borrar-usuario
  [])