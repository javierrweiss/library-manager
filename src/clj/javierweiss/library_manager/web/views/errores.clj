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
   [:h2 "¡Lo sentimos! ¡El acceso se encuentra restringido!"]])

(defcomponent ^:endpoint login_intento_fallido [_]
  [:div#failed
   [:h2 "¡Lo sentimos! Contraseña o usuario incorrecto. Intente de nuevo."]])

(defcomponent ^:endpoint usuario_inexistente [_]
  [:div#usuario_inexistente
   [:h2 "¡Lo sentimos! No existe un usuario registrado con esos datos"]])

(defcomponent ^:endpoint login_unsuccessful [_]
  [:div#forbidden_login
   [:h2 "¡Lo sentimos! ¡Ha superado la cantidad de intentos permitidos!"]])