(ns javierweiss.library-manager.web.views.exito
  (:require [simpleui.core :as simpleui :refer [defcomponent]]))

(defcomponent ^:endpoint exito [_ mensaje]
  [:div#operacion_exitosa
   [:h2 "¡Éxito!"]
   [:p mensaje]])