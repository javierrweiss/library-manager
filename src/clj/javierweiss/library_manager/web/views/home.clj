(ns javierweiss.library-manager.web.views.home
  (:require [javierweiss.library-manager.web.views.usuario :refer [registro_usuario]]
            [javierweiss.library-manager.web.views.login :refer [login]]
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
    [:div [:span.text-dark-purple "Facebook"] [:img {:src "img/facebook.png" :alt "Facebook" :style "width: 1.5rem; height: 1.5rem"}]] 
    [:div [:span.text-dark-purple "X"] [:img {:src "img/x.png" :alt "X" :style "width: 1.5rem; height: 1.5rem"}]] 
    [:div [:span.text-dark-purple "Instagram"] [:img {:src "img/instagram.png" :alt "Instagram" :style "width: 1.5rem; height: 1.5rem"}]] 
    [:div [:span.text-dark-purple "@javierweiss2024"]]]])

(defcomponent ^:endpoint body [_]
  [:div#hero {:style "background-image: url('/img/libros.jpg')"}
   [:div.bg-slate-blue {:style "width: 50%; margin: 6rem; padding: 3rem"}
    [:h1 {:style "font-size: 2.75rem; color: #E5D4ED"} "¿Quieres gestionar todo lo que necesitas para tu investigación en un mismo lugar?"] 
    [:h2.p-1 {:style "font-size: 2rem; color: #E5D4ED"} "Entonces " [:b "CiteFlow"] " es para tí"]
    [:h3.p-6 {:style "font-size: 1.25rem; color: #E5D4ED"} "¡Abre una cuenta ahora! ¡Es gratis!"]
    [:div.flex.justify-center.gap-5
     [:button.flex.font-semibold.p-6.bg-rebecca-purple 
      {:style "width: 18%; height: 2.5%; border-radius: 0.375rem"
       :type "button"
       :hx-get "registro_usuario"
       :hx-target "#hero"
       :hx-swap "outerHTML"}
      "Regístrate"]
     [:button.flex.font-semibold.p-6.bg-rebecca-purple 
      {:style "width: 18%; height: 2.5%; border-radius: 0.375rem; text-align: center"
       :type "button"
       :hx-get "/usuario/login"
       :hx-target "#hero"
       :hx-swap "outerHTML"}
      "Ingresa"]]]])

(defcomponent ^:endpoint home [req]
  registro_usuario
  login
  [:section#home
   [:div.grid
    (navbar req)
    (body req)
    (footer req)]])

(defn home-routes
  [base-path]
  (simpleui/make-routes
   base-path
   (fn [req]
     (page-htmx
      (home req)))))
 
(comment
  
  (page-htmx (login { }"" ""))
  
  (page-htmx (registro_usuario {:params {:cuenta ""
                                         :clave ""
                                         :nombre ""
                                         :correo ""}}))
  
(simpleui/make-routes
 "/usuario"
 (fn [req]
   (page-htmx
    (home req)))) 
(into [] (concat (simpleui/make-routes
                  ""
                  (fn [req]
                    (page-htmx
                     (home req))))
                 (simpleui/make-routes
                  "/ingreso"
                  (fn [req]
                    (login req "" "")))))
  
  )