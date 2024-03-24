(ns javierweiss.library-manager.web.routes.api
  (:require
    [integrant.core :as ig]
    [javierweiss.library-manager.web.controllers.health :as health]
    [javierweiss.library-manager.web.controllers.usuario :as usuario]
    [javierweiss.library-manager.web.middleware.exception :as exception]
    [javierweiss.library-manager.web.middleware.formats :as formats]
    [reitit.coercion.malli :as malli]
    [reitit.ring.coercion :as coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [reitit.swagger :as swagger]
    [integrant.repl.state :as state]))

(def default-routes
 [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title "javierweiss.library-manager API"}}
           :handler (swagger/create-swagger-handler)}}]
  ["/health"
   {:get health/healthcheck!}]])
  
(def usuario-routes 
  [["/usuario"
    {:post {:parameters {:params {:nombre string?
                                  :cuenta string?
                                  :correo string?
                                  :clave string?}}
            :handler usuario/crear-usuario}}]
   ["/usuario/:id" {:get {:parameters {:query {:id string?}}
                          :handler usuario/obtener-usuario-por-id}}]
   ["/usuario/del/"
    {:delete {:parameters {:query {:id string?}}
              :handler usuario/borrar-usuario}}]
   ["/usuario/nom/"
    {:put {:parameters {:query {:id string?
                                :usuario_nombre string?}}
           :handler usuario/actualizar-nombre-usuario}}]
   ["/usuario/mail/"
    {:put {:parameters {:query {:id string?
                                :usuario_correo string?}}
           :handler usuario/actualizar-correo-usuario}}]
   ["/usuario/kywd/"
    {:put {:parameters {:query {:id string?
                                :usuario_clave string?}}
           :handler usuario/actualizar-clave-usuario}}]
   ["/usuario/todos/"
    {:get usuario/obtener-todos-usuarios}]
   ["/usuario/login"
    {:post {:parameters {:params {:cuenta string?
                                  :clave }}}}]])

(defn version1-api
  "Recibe uno o más vectores de vectores representando rutas y las hace preceder del path v1"
  [& rutas]
  (into ["/v1"] cat rutas))
 
;; Routes
(def api-routes (conj default-routes (version1-api usuario-routes)))

 
(def route-data {:coercion   malli/coercion
                 :muuntaja   formats/instance
                 :swagger    {:id ::api}
                 :middleware [;; query-params & form-params
                              parameters/parameters-middleware
                              ;; content-negotiation
                              muuntaja/format-negotiate-middleware
                              ;; encoding response body
                              muuntaja/format-response-middleware
                              ;; exception handling
                              coercion/coerce-exceptions-middleware
                              ;; decoding request body
                              muuntaja/format-request-middleware
                              ;; coercing response bodys
                              coercion/coerce-response-middleware
                              ;; coercing request parameters
                              coercion/coerce-request-middleware
                              ;; exception handling
                              exception/wrap-exception]})

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (fn [] [base-path (merge opts route-data) api-routes]))


(comment
  
  (count usuario-routes)    
  (version1-api usuario-routes [["ruta/x" {}] ["ruta/y" {}]])
  (tap> ((ig/init-key :reitit.routes/api state/system)))
  
  )   