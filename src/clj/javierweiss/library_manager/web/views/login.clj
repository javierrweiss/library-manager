(ns javierweiss.library-manager.web.views.login
  (:require [simpleui.core :as simpleui :refer [defcomponent]]))

(defcomponent ^:endpoint login [req ^:path cuenta ^:path clave]
  [:div
   [:form {:hx-post ""}
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