(ns javierweiss.library-manager.web.routes.utils
  (:require [clojure.set :refer [rename-keys]])
  (:import (org.springframework.security.crypto.argon2 Argon2PasswordEncoder)))

(def route-data-path [:reitit.core/match :data :routes])

(defn route-data
  [req]
  (->> (get-in req route-data-path)
      flatten
      (filter map?)
       (apply merge)))

(defn route-data-key
  [req k]
  (get-in req (conj route-data-path k)))

(def encoder (Argon2PasswordEncoder. 16 64 1 60000 5))

(defn hash-string
  "https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/crypto/argon2/Argon2PasswordEncoder.html"
  [str] 
  (.encode encoder str))

 (defn traverse-db-result
   "Traverses a nested data structure and returns first non-sequential element."
   [ds]
   (loop [ds (if (set? ds) (seq ds) ds)]
     (if (or (map? ds) (not (coll? ds)))
       ds
       (recur (first ds)))))

(defn obtener-datos-usuario
  [usuario]
  (if (every? #{:id :correo :nombre :cuenta} (keys usuario))
    usuario
    (rename-keys usuario {:xt/id :id 
                          :usuario/nombre :nombre 
                          :usuario/correo :correo 
                          :usuario/clave :clave
                          :usuario/cuenta :cuenta})))

(comment 
  
  (def h (hash-string "Esta es mi clave, caramba!"))
  
  (.matches encoder "Esta es mi clave, caramba!" h)
  
  (.getMethods (.getClass h))

  (def y (Argon2PasswordEncoder. 16 64 1 60000 5))

  (.encode y "Hola polizon")
  )