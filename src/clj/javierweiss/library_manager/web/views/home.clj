(ns javierweiss.library-manager.web.views.home
  (:require [javierweiss.library-manager.web.views.usuario :refer [registro_usuario]]
            [javierweiss.library-manager.web.htmx :refer [page-htmx]]
            [simpleui.core :as simpleui :refer [defcomponent]]))

(defcomponent ^:endpoint navbar [_]
  [:nav.bg-pale-purple.p-12
   [:div.flex.flex-row.gap-5 
    [:div.basis-2 [:h4.text-davys-gray [:a {:href ""} "Home"]]]
    [:div.basis-2 [:h4.text-davys-gray [:a {:href ""} "Mis Bibliotecas"]]]
    [:div.basis-2 [:h4.text-davys-gray [:a {:href ""} "How-to"]]]
    [:div.basis-2 [:h4.text-davys-gray [:a {:href ""} "Buscar"]]]
    [:div.basis-2 [:h4.text-davys-gray [:a {:href ""} "Sobre nosotros"]]]]])

(defcomponent ^:endpoint home [req]
  registro_usuario
  [:section#home
   [:div (navbar req)]
   [:div {:style "background-image: url('/img/libros.jpg')"}
    [:div.bg-dark-purple.border.border-3
     [:h1.text-9xl.text-yellow-400.p-6 "¿Quieres gestionar todo lo que necesitas para tu investigación en un mismo lugar? Entonces "
      [:b.decoration-pale-purple "CiteFlow"] " es para tí"]
     [:h3.text-6xl.text-yellow-400.p-6 "¡Abre una cuenta ahora! ¡Es gratis!"]]
    [:div.grid.grid-cols-2.gap-5 
     [:button.font-semibold.p-6.bg-rebecca-purple
      {:type "button"
       :hx-get "registro_usuario"
       :hx-target "#home"
       :hx-swap "outerHTML"}
      "Registrate"]
     [:button.font-semibold.p-6.bg-rebecca-purple
      {} "Ingresa"]]]])

(defn ui-routes
  [base-path]
  (simpleui/make-routes
   base-path
   (fn [req]
     (page-htmx
      (home req)))))