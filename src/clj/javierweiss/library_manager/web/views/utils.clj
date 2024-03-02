(ns javierweiss.library-manager.web.views.utils
  (:require [simpleui.core :as simpleui :refer [defcomponent]]
            [javierweiss.library-manager.web.htmx :refer [page-htmx]]))

(defcomponent ^:endpoint bad_request [req msj]
  [:div#bad_request
   [:h3 "Hubo un problema"]
   [:p msj]])

(defcomponent ^:endpoint recurso_creado [req]
  [:div
   [:h3 "Recurso creado exitosamente"]])

(defn ui-routes
  [base-path]
  (simpleui/make-routes
   base-path
   (fn [req]
     (page-htmx
      (bad_request req "")
      (recurso_creado req)))))