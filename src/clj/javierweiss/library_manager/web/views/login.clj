(ns javierweiss.library-manager.web.views.login
  (:require [simpleui.core :as simpleui :refer [defcomponent make-routes]]
            [javierweiss.library-manager.web.htmx :refer [page-htmx]]))

(defcomponent ^:endpoint login [req ^:path cuenta ^:path contrasena ^:path correo]
  [:div#login
   [:form {:hx-post "/api/v1/usuario_login"}
    [:label {:for "cuenta"} "Cuenta"]
    [:input {:type "text"
             :required true
             :maxlength "40"
             :value cuenta
             :name (path cuenta)}]
    [:label {:for "correo"} "Correo electrónico"]
    [:input {:type "email"
             :required true 
             :value correo 
             :name (path correo)}]
    [:label {:for "contrasena"} "Contraseña"]
    [:input {:type "password"
             :required true
             :maxlength "100"
             :value contrasena
             :name (path contrasena)}]
    [:button.flex.font-semibold.p-6.bg-rebecca-purple
     {:style "width: 18%; height: 2.5%; border-radius: 0.375rem; text-align: center"
      :type "submit"}
     "Ingresar"]]])

(defn login-route
  []
  (make-routes
   "/usuario/login"
   (fn [req]
     (page-htmx
      (login req "" "" "")))))

;; El hasheo y conversión a string de las claves debería ocurrir enteramente en el cliente, para que no viaje ninguna clave decodificada por la red

(comment
  (require '[javierweiss.library-manager.web.htmx :refer [page-htmx]])

  (page-htmx (login {:params {:cuenta ""
                              :clave ""}} " " ""))
  
  (page-htmx (login {:params {:cuenta ""
                              :clave ""}} ))
  
  )