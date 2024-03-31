(ns javierweiss.library-manager.web.security.loginsystem
  (:require [taoensso.tempel :as tempel] 
            [javierweiss.library-manager.web.security.adminconfig :refer [admin-public-key]]
            [javierweiss.library-manager.web.views.errores :as err]
            [javierweiss.library-manager.web.htmx :as htmx]))
  
  (defonce password-rate-limiter
  ;; Basic in-memory rate limiter to help protect against brute-force
  ;; attacks from the same user-id. In real applications you'll likely
  ;; want a persistent rate limiter for user-id, IP, etc.
  (tempel/rate-limiter
    {"1 attempt/s per 5 sec/s" [1        5000]
     "2 attempt/s per 1 min/s" [2 (* 1 60000)]
     "5 attempt/s per 5 min/s" [5 (* 5 60000)]}))
  
  (defn logear-usuario
    "Recibe id de usuario, keychain codificado (guardado como clave) y la contraseña introducida por el usuario de la UI"
    [id kych pswd]
    (tempel/with-min-runtime 2000
      (if-let [rl (password-rate-limiter id)]
        (throw (ex-info "El usuario ha superado la cantidad de intentos permitidos" {:limit-info rl}))
        (or 
         (when kych
           (let [decrypted-keychain (tempel/keychain-decrypt kych {:password pswd})]
             (password-rate-limiter :rl/reset id)
             decrypted-keychain))
         (:body (htmx/page-htmx (err/login_intento_fallido {})))))))