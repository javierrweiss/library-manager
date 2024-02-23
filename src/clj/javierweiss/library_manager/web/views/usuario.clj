(ns javierweiss.library-manager.web.views.usuario
  (:require
   [simpleui.core :as simpleui :refer [defcomponent]]
   [javierweiss.library-manager.web.htmx :refer [page-htmx]]
   [clojure.tools.logging :as log]
   [javierweiss.library-manager.db.db :as db]
   [javierweiss.library-manager.web.routes.utils :as utils]))

(defcomponent ^:endpoint registro_exitoso [req]
  [:div#registro_exitoso "Registro creado con éxito"
   #_[:h2 ]])

(defcomponent ^:endpoint registro_no_exitoso [req msj]
  [:div#registro_no_exitoso
   [:h2 msj]])

(defcomponent ^:endpoint registro_usuario [req ^:path nombre ^:path correo ^:path cuenta ^:path clave] 
  [:div#user-register
   [:form {:id "registro"
           :hx-post "/api/v1/usuario"
           :hx-patch "registro-exitoso"
           :hx-swap "#registro_exitoso"}
    [:label "Nombre Completo"]
    [:input {:type "text"
             :name  (path "nombre")
             :value nombre}][:br]
    [:label "Correo electrónico"]
    [:input {:type "text"
             :name  (path "correo")
             :value correo}][:br]
    [:label "Nombre de cuenta"]
    [:input {:type "text"
             :name  (path "cuenta")
             :value cuenta}][:br]
    [:label "Ingrese su contraseña"]
    [:input {:type "password"
             :name (path "clave")
             :value clave}][:br]
    [:input {:type "submit"
             :value "Registrarse"}]]
             (registro_exitoso req)])


(defn ui-routes
  [base-path]
  (simpleui/make-routes
    base-path
    (fn [req]
      (page-htmx
        (registro_usuario req "" "" "" "")))))


(comment
  [:label {:style "margin-right: 10px"}
   "What is your name?"]
  [:input {:type "text"
           :name "my-name"
           :hx-patch "hello"
           :hx-target "#hello"
           :hx-swap "outerHTML"}]
  (ui-routes "")
  
  
  
  )
