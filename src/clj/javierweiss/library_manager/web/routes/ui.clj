(ns javierweiss.library-manager.web.routes.ui
  (:require
   [integrant.core :as ig]
   [javierweiss.library-manager.web.middleware.exception :as exception]
   [javierweiss.library-manager.web.middleware.formats :as formats]
   [javierweiss.library-manager.web.views.home :refer [home-routes]]
   [javierweiss.library-manager.web.views.login :refer [login-route]]
   [javierweiss.library-manager.web.views.usuario :refer [usuario-route]]
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
  [base-path (route-data opts) (home-routes base-path) (login-route) (usuario-route)])
