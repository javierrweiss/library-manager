(ns javierweiss.library-manager.web.routes.ui
  (:require
   [integrant.core :as ig]
   [javierweiss.library-manager.web.middleware.exception :as exception]
   [javierweiss.library-manager.web.middleware.formats :as formats]
   [javierweiss.library-manager.web.views.usuario :as usuario]
   [javierweiss.library-manager.web.views.utils :as utils-ui]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]))


(defn route-data
  [opts]
  (merge
    opts
    {:muuntaja   formats/instance
     :middleware
     [;; Default middleware for ui
      ;; query-params & form-params
      parameters/parameters-middleware
      ;; encoding response body
      muuntaja/format-response-middleware
      ;; exception handling
      exception/wrap-exception]}))

(derive :reitit.routes/ui :reitit/routes)

(defmethod ig/init-key :reitit.routes/ui
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  [base-path (route-data opts) (usuario/ui-routes "/usuario") (utils-ui/ui-routes "/uts")])
