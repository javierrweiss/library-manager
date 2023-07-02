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
    [reitit.swagger :as swagger]))


;; Routes
(defn api-routes
  [_opts]
  [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title "javierweiss.library-manager API"}}
           :handler (swagger/create-swagger-handler)}}]
   ["/health"
    {:get health/healthcheck!}]
   ["/usuario"
    {:get {:parameters {:query {:id number?}}}
     :handler usuario/obtener-usuario}
    {:post {:parameters {:form-params {:nombre string?
                                       :cuenta string?
                                       :correo string?
                                       :clave string?}}} 
     :handler usuario/crear-usuario}
    {:put {:parameters {:query {:id number?}
                        :form-params {:nombre string?}}}
     :handler usuario/actualizar-nombre-usuario}
    {:put {:parameters {:query {:id number?}
                        :form-params {:correo string?}}}
     :handler usuario/actualizar-correo-usuario}
    {:put {:parameters {:query {:id number?}
                        :form-params {:clave string?}}}
     :handler usuario/actualizar-clave-usuario}
    {:delete {:parameters {:query {:id number?}}
              :handler usuario/borrar-usuario}}]])

 
(defn route-data
  [opts]
  (merge
    opts
    {:coercion   malli/coercion
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
                  exception/wrap-exception]}))


(derive :reitit.routes/api :reitit/routes)


(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  [base-path (route-data opts) (api-routes opts)])
