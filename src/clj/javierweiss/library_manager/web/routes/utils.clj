(ns javierweiss.library-manager.web.routes.utils)

(def route-data-path [:reitit.core/match :data :routes 0 1])

(defn route-data
  [req]
  (get-in req route-data-path))

(defn route-data-key
  [req k]
  (get-in req (conj route-data-path k)))
