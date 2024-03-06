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
  [:div.border-solid.border-4.border-indigo-600.mx-80.my-12.px-40.bg-slate-blue
   [:h2.p-8.text-center "¡Complete este formulario para crear su cuenta!"]
   [:form#user-register.object-center.p-6
    {:hx-post "/api/v1/usuario"
     :hx-target "this"
     :hx-swap "outerHTML"}
    [:div.grid.grid-cols-2.gap-5.justify-items-center
     [:label.pl-35 {:for "nombre"} "Nombre Completo"]
     [:input.pr-35
      {:id "nombre"
       :type "text"
       :name  (path "nombre")
       :value nombre}]
     [:label.pl-35 "Correo electrónico"]
     [:input.pr-35
      {:type "text"
       :name  (path "correo")
       :value correo}]
     [:label.pl-35 "Nombre de cuenta"]
     [:input.pr-35
      {:type "text"
       :name  (path "cuenta")
       :value cuenta}]
     [:label.pl-35 "Ingrese su contraseña"]
     [:input.pr-35
      {:type "password"
       :name (path "clave")
       :value clave}]]
    [:br.p-4]
    [:div.flex.justify-center.p-4
     [:input.p-4.form-input.rounded-full.bg-blue-400.text-white-200
      {:type "submit"
       :value "Registrarse"}]]]])

(defcomponent ^:endpoint titulo [_]
  [:h1.p-2.text-black-700.font-bold.text-2xl.text-center "Nuevo Usuario"])

(defn ui-routes
  [base-path]
  (simpleui/make-routes
   base-path
   (fn [req]
     (page-htmx
      (titulo req)
      (registro_usuario req "" "" "" "")))))


(comment
  
  :dbg 
  
  )
