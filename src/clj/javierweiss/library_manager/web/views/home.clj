(ns javierweiss.library-manager.web.views.home
  (:require [javierweiss.library-manager.web.views.usuario :refer [registro_usuario]]
            [javierweiss.library-manager.web.htmx :refer [page-htmx]]
            [simpleui.core :as simpleui :refer [defcomponent]]))

(defcomponent ^:endpoint navbar [_]
  [:nav.bg-pale-purple.p-12
   [:div.flex.flex-row.gap-5.justify-end
    [:div  [:a.text-dark-purple {:href ""} "Home"]]
    [:div  [:a.text-dark-purple {:href ""} "Mis Bibliotecas"]]
    [:div  [:a.text-dark-purple {:href ""} "How-to"]]
    [:div  [:a.text-dark-purple {:href ""} "Buscar"]]
    [:div  [:a.text-dark-purple {:href ""} "Sobre nosotros"]]]])

(defcomponent ^:endpoint footer [_]
  [:footer.bg-pale-purple.p-12 
   [:div.grid.gap-5.justify-center
    [:div.text-dark-purple "Facebook"] 
    [:div.text-dark-purple "Twitter"] 
    [:div.text-dark-purple "Instagram"] 
    [:div.text-dark-purple "@javierweiss2024"]]])

(defcomponent ^:endpoint body [_]
  [:div {:style "background-image: url('/img/libros.jpg')"}
   [:div.bg-slate-blue {:style "width: 50%; margin: 6rem; padding: 3rem"}
    [:h1 {:style "font-size: 2.75rem; color: #E5D4ED"} "¿Quieres gestionar todo lo que necesitas para tu investigación en un mismo lugar?"] 
    [:h2.p-1 {:style "font-size: 2rem; color: #E5D4ED"} "Entonces " [:b "CiteFlow"] " es para tí"]
    [:h3.p-6 {:style "font-size: 1.25rem; color: #E5D4ED"} "¡Abre una cuenta ahora! ¡Es gratis!"]
    [:div.flex.justify-center.gap-5
     [:button.flex.font-semibold.p-6.bg-rebecca-purple 
      {:style "width: 25%; height: 2.5%; border-radius: 0.375rem"
       :type "button"
       :hx-get "registro_usuario"
       :hx-target "#home"
       :hx-swap "outerHTML"}
      "Regístrate"]
     [:button.flex.font-semibold.p-6.bg-rebecca-purple 
      {:style "width: 25%; height: 2.5%; border-radius: 0.375rem"
       :type "button"
       :hx-get ""
       :hx-target "#home"
       :hx-swap "outerHTML"}
      "Ingresa"]]]])

(defcomponent ^:endpoint home [req]
  registro_usuario
  [:section#home
   [:div.grid
    (navbar req)
    (body req)
    (footer req)]])

(defn ui-routes
  [base-path]
  (simpleui/make-routes
   base-path
   (fn [req]
     (page-htmx
      (home req)))))