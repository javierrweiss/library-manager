(ns javierweiss.library-manager.web.htmx
  (:require
    [simpleui.render :as render] 
    [hiccup.page :as p]
    [ring.util.http-response :as http-response]))

(defn page
  [opts & content]
  (-> (p/html5 opts content)
      http-response/ok
      (http-response/content-type "text/html")))

(defn page-htmx
  [& body]
  (page
    [:head
     [:meta {:charset "UTF-8" :name "viewport" :content "width=device-width, initial-scale=1.0"}]
     [:title "Library Manager"] 
     [:link {:rel "stylesheet" :href "output.css" :type "text/css"}]
     [:script {:src "https://unpkg.com/htmx.org@1.2.0/dist/htmx.min.js" :defer true}]]
    [:body (render/walk-attrs body)]))


(comment
  
  
  )