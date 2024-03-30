(ns javierweiss.library-manager.web.views.login
  (:require [simpleui.core :as simpleui :refer [defcomponent make-routes]]
            [javierweiss.library-manager.web.htmx :refer [page-htmx]]))

(defcomponent ^:endpoint login [req ^:path cuenta ^:path clave]
  [:div#login
   [:form {:hx-post "/api/v1/login"}
    [:input {:type "text"
             :required true
             :maxlength "40"
             :value cuenta
             :name (path cuenta)}]
    [:input {:type "password"
             :required true
             :maxlength "100"
             :value clave 
             :name (path clave)}]
    [:button {:type "submit"
              :value "Ingresar"}]]])

(defn login-route
  []
  (make-routes
   "/usuario/login"
   (fn [req]
     (page-htmx
      (login req "" "")))))

;; El hasheo y conversión a string de las claves debería ocurrir enteramente en el cliente, para que no viaje ninguna clave decodificada por la red

(comment
  (require '[javierweiss.library-manager.web.htmx :refer [page-htmx]])

  (page-htmx (login {:params {:cuenta ""
                              :clave ""}} " " ""))
  
  (page-htmx (login {:params {:cuenta ""
                              :clave ""}} ))
  
  )