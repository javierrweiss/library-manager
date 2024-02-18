(ns javierweiss.library-manager.web.views.registro-usuario
  (:require
   [ctmx.core :as ctmx :refer [defcomponent]]
   [javierweiss.library-manager.web.htmx :refer [page-htmx]]
   [clojure.tools.logging :as log]
   [javierweiss.library-manager.db.db :as db]
   [javierweiss.library-manager.web.routes.utils :as utils]))

(defcomponent ^:endpoint usuario [req ^:path nombre ^:path correo ^:path cuenta ^:path clave]
  [:div#user-register
   [:form {:id "registro"
           :hx-post "/api/v1/usuario"}
    [:label "Nombre Completo"]
    [:input {:type "text"
             :name  (path "nombre")
             :value nombre}]
    [:label "Correo electrónico"]
    [:input {:type "text"
             :name  (path "correo")
             :value correo}]
    [:label "Nombre de cuenta"]
    [:input {:type "text"
             :name  (path "cuenta")
             :value cuenta}]
    [:label "Ingrese su contraseña"]
    [:input {:type "password"
             :name (path "clave")
             :value clave}]
    [:input {:type "submit"
             :value "Registrarse"}]]])


(defn ui-routes
  [base-path]
  (ctmx/make-routes
    base-path
    (fn [req]
      (page-htmx
        (usuario req "" "" "" "")))))


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
