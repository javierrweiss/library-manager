(ns javierweiss.library-manager.web.views.hello
    (:require
      [ctmx.core :as ctmx :refer [defcomponent]]
      [javierweiss.library-manager.web.htmx :refer [page-htmx]]))

(defcomponent ^:endpoint hello [req my-name]
  [:div#hello "Hello " my-name])

(defcomponent ^:endpoint crea-usuario [req nombre correo cuenta clave]
  [:div#user-register
   [:form 
    [:label "Nombre Completo"]
    [:input {:type "text"
             :name  nombre}]
    [:label "Correo electrónico"]
    [:input {:type "text"
             :name  correo}]
    [:label "Nombre de cuenta"]
    [:input {:type "text"
             :name  cuenta}]
    [:label "Ingrese su contraseña"]
    [:input {:type "password"
             :name clave}]
    [:input {:type "submit"
             :value "Registrarse"
             :hx-post "/api/usuario"}]]])

(defn ui-routes [base-path]
  (ctmx/make-routes
   base-path
   (fn [req]
     (page-htmx
      (crea-usuario req "" "" "" "")))))


(comment 
  [:label {:style "margin-right: 10px"}
   "What is your name?"]
[:input {:type "text"
         :name "my-name"
         :hx-patch "hello"
         :hx-target "#hello"
         :hx-swap "outerHTML"}]
  
  )