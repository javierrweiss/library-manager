(ns javierweiss.library-manager.web.views.errores
  (:require [simpleui.core :as simpleui :refer [defcomponent]]))

(defcomponent ^:endpoint error [_ mensaje]
  [:div#error
   [:h2 "¡Lo sentimos! Hubo un problema"]
   [:p mensaje]])

(defcomponent ^:endpoint error_servidor [_]
  [:div#error_servidor
   [:h2 "¡Ups! Algo ha salido muy mal"]])

(defcomponent ^:endpoint bad_request [_]
  [:div#bad_request
   [:h2 "Los datos proporcionados no han podido ser validados"]])

(defcomponent ^:endpoint no_encontrado [_]
  [:div#not_found
   [:h2 "¡Lo sentimos! No hemos podido encontrar lo que está buscando"]])

(defcomponent ^:endpoint no_autorizado [_]
  [:div#not_authorized
   [:h2 "Usted no se encuentra autorizado para acceder a esta página"]])

(defcomponent ^:endpoint prohibido [_]
  [:div#forbidden
   [:h2 "¡Lo sentimos! ¡El acceso se encuentra restringido"]])