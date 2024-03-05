(ns javierweiss.library-manager.web.views.usuario
  (:require
   [simpleui.core :as simpleui :refer [defcomponent]]
   [javierweiss.library-manager.web.htmx :refer [page-htmx]]
   [clojure.tools.logging :as log]
   [javierweiss.library-manager.db.db :as db]
   [javierweiss.library-manager.web.routes.utils :as utils]))

(defcomponent ^:endpoint registro_exitoso [req]
  [:div#registro_exitoso 
   [:h2 "Registro creado con éxito"]])

(defcomponent ^:endpoint registro_no_exitoso [req msj]
  [:div#registro_no_exitoso
   [:h2 msj]])

(defcomponent ^:endpoint registro_usuario [req ^:path nombre ^:path correo ^:path cuenta ^:path clave]
  [:div.w-full.max-w-xs.bg-black
   [:form#user-register
    {:hx-post "/api/v1/usuario"
     :hx-target "this"
     :hx-swap "outerHTML"} 
    [:h2 "Complete este formulario para crear su cuenta"] 
    [:div
     [:label {:for "nombre"} "Nombre Completo"] 
     [:input.form-input.px-4.py-3.rounded-full.bg-red-400
      {:id "nombre"
       :type "text"
       :name  (path "nombre")
       :value nombre}]]
    [:div
     [:label "Correo electrónico"]
     [:input
      {:type "text"
       :name  (path "correo")
       :value correo}]]
    [:div
     [:label "Nombre de cuenta"]
     [:input
      {:type "text"
       :name  (path "cuenta")
       :value cuenta}]]
    [:div
     [:label"Ingrese su contraseña"]
     [:input
      {:type "password"
       :name (path "clave")
       :value clave}]]
    [:input
     {:type "submit"
      :value "Registrarse"}]]])

(defcomponent ^:endpoint tes [_]
  [:h1.text-red-700.font-serif "Hola mundo!!!"])

(defn ui-routes
  [base-path]
  (simpleui/make-routes
   base-path
   (fn [req]
     (page-htmx
      (tes req)
      (registro_usuario req "" "" "" "")))))


(comment
  
  :dbg 
  
  )
