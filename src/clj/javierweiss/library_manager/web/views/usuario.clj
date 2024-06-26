(ns javierweiss.library-manager.web.views.usuario
  (:require
   [simpleui.core :as simpleui :refer [defcomponent make-routes]]
   [javierweiss.library-manager.web.htmx :refer [page-htmx]]
   [clojure.tools.logging :as log]
   [javierweiss.library-manager.db.db :as db]
   [javierweiss.library-manager.web.routes.utils :as utils]))

(defcomponent ^:endpoint registro_no_exitoso [_ msj]
  [:div#registro_no_exitoso
   [:h2 msj]])

(defcomponent ^:endpoint formulario_usuario [req ^:path nombre ^:path correo ^:path cuenta ^:path clave]
  [:section#registro 
   [:div.border-solid.border-4.mx-80.my-12.px-40.bg-slate-blue
    [:h2.p-8.text-center "¡Complete este formulario para crear su cuenta!"]
    [:form#user-register.object-center.p-6
     {:hx-post "/api/v1/usuario"
      :hx-target "#registro"
      :hx-swap "outerHTML"}
     [:div.grid.grid-cols-2.gap-5.justify-items-center
      [:label.pl-35 {:for "nombre"} "Nombre Completo"]
      [:input.pr-35
       {:id "nombre"
        :type "text"
        :name  (path "nombre")
        :value nombre
        :required true
        :maxlength "60"}]
      [:label.pl-35 "Correo electrónico"]
      [:input.pr-35
       {:type "email"
        :name  (path "correo")
        :value correo
        :required true
        :maxlength "120"}]
      [:label.pl-35 "Nombre de usuario"]
      [:input.pr-35
       {:type "text"
        :name  (path "cuenta")
        :value cuenta
        :required true
        :maxlength "40"}]
      [:label.pl-35 "Ingrese su contraseña"]
      [:input.pr-35
       {:type "password"
        :name (path "clave")
        :value clave
        :required true
        :maxlength "100"}]]
     [:br.p-4]
     [:div.flex.justify-center.p-4
      [:input.p-4.form-input.rounded-full.bg-pale-purple.text-white-200
       {:type "submit"
        :value "Registrarse"}]]]]])

(defcomponent ^:endpoint titulo [_] 
  [:h1.p-6.text-black-700.font-bold.text-2xl.text-center "Nuevo Usuario"])

(defcomponent ^:endpoint muestra_usuario [req]
  [:div#muestra_usuario])

(defcomponent ^:endpoint registro_usuario [req] 
  [:article.bg-rebecca-purple.p-8
   (titulo req)
   (formulario_usuario req)])

(defn usuario-route
  []
  (make-routes
   "/usuario/registro"
   (fn [req]
     (page-htmx
      (registro_usuario req)))))

(comment

  :dbg

  (ui-routes "/")
  
  (require '[javierweiss.library-manager.web.htmx :refer [page-htmx]])

  (page-htmx (formulario_usuario {} "" "" " " ""))

  (let [req {}]
    (page-htmx
     (titulo req)
     (registro_usuario req "" "" "" "")))

  (simpleui/make-routes
   "/"
   ["/rutax" "/rutay"]
   (fn [req]
     (page-htmx
      [:header.p-12.bg-pale-purple
       [:div
        [:h2 "Acá va mi menú"]]]
      [:article.bg-rebecca-purple.p-8
       (titulo req)
       (registro_usuario req)]
      [:footer.bg-pale-purple.relative.p-12
       [:div "@javierweiss2024"]])))
  )
