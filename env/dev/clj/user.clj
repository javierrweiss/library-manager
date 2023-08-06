(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require 
    [clojure.spec.alpha :as s]
    [clojure.tools.namespace.repl :as repl]
    [criterium.core :as c]                                  ;; benchmarking
    [expound.alpha :as expound]
    [integrant.core :as ig]
    [integrant.repl :refer [clear go halt prep init reset reset-all]]
    [integrant.repl.state :as state]
    [kit.api :as kit]
    [lambdaisland.classpath.watch-deps :as watch-deps]      ;; hot loading for deps 
    [javierweiss.library-manager.core :refer [start-app]])) ;; Si falta esta dependencia, no arranca el repl
 
;; uncomment to enable hot loading for deps 
(watch-deps/start! {:aliases [:dev :test]})

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn dev-prep!
  []
  (integrant.repl/set-prep! (fn []
                              (-> (javierweiss.library-manager.config/system-config {:profile :dev})
                                  (ig/prep)))))

(defn test-prep!
  []
  (integrant.repl/set-prep! (fn []
                              (-> (javierweiss.library-manager.config/system-config {:profile :test})
                                  (ig/prep)))))

;; Can change this to test-prep! if want to run tests as the test profile in your repl
;; You can run tests in the dev profile, too, but there are some differences between
;; the two profiles.
(dev-prep!)

(repl/set-refresh-dirs "src/clj" "test/clj")

(def refresh repl/refresh)

(defn reset-db []
  (migratus.core/reset (:db.sql/migrations state/system)))

(defn rollback []
  (migratus.core/rollback (:db.sql/migrations state/system)))

(defn migrate []
  (migratus.core/migrate (:db.sql/migrations state/system)))

(def query-fn (:db.sql/query-fn state/system)) 
     
(comment 
  (require '[portal.api :as p]) 
  (def p (p/open {:launcher :vs-code}))
  (add-tap #'p/submit)  
  (test-prep!)   
  (dev-prep!)   
  (init)  
  ;; Iniciar el sistema sin tocar ninguna conexión SQL 
  (go [:db-type/xtdb :repl/server :server/http :reitit.routes/api :reitit.routes/ui])   
  (go)               
  (halt)      
  (reset)          
  (reset-all)     
  (clear) 
  (refresh)  ;;Hay que refrescar para que escanee los archivos fuente de nuevo.
  (ns-unmap 'user 'start-app)  
  (:db.sql/connection state/system)
  
  (query-fn :obtener-referencias-y-publicaciones)

  ;;Crear las tablas en el orden correcto y sin que que solape el timestamp
  (doseq [tabla ["usuarios-table"
                 "autores-table"
                 "referencias-table"
                 "publicaciones-table"
                 "citas-table"
                 "comentarios-table"
                 "colecciones-table"
                 "bibliotecas-table" 
                 "biblioteca-items-table"
                 "coleccion-items-table"]]
    (migratus.core/create (:db.sql/migrations state/system) tabla)
    (Thread/sleep 1000))

  (kit/sync-modules)
  (kit/list-modules)
  (kit/install-module :kit/ctmx)
  (kit/install-module :kit/hato)
  ;;No necesitaba crearla, ya estaba
  (def consulta (:db.sql/query-fn state/system)) 

  (consulta :crear-usuario! {:usuarios/correo "leonardoblanco@gmail.com"
                             :usuarios/nombre "Leonardo Blanco"
                             :usuarios/cuenta "leoblanco"
                             :usuarios/clave "El leo!! 2004"})

  (consulta :obtener-por-id {:table "usuarios"
                             :id #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b"})

  (consulta :borrar-por-id! {:table "usuarios"
                             :id #uuid "1a9bc41a-3990-4d6e-9025-4c21869be868"})

  (consulta :obtener-todo {:table "usuarios"})
  (consulta :obtener-todo {:table "bibliotecas"})
  (consulta :obtener-todo {:table "biblioteca_items"}) 
  (consulta :obtener-todo {:table "autores"})
  (consulta :obtener-todo {:table "comentarios"})
  (consulta :obtener-todo {:table "referencias"})
  

  (consulta :crear-autor! {:autores/nombres "Reinhart"
                           :autores/apellidos "Koselleck"})
  
  (consulta :crear-autor! {:autores/nombres "Leonhard"
                           :autores/apellidos "Bech"})
  
  (consulta :crear-referencia! {:referencia/autores #uuid "961469a7-ff35-47a7-b136-9a807a7666a9"
                                :referencia/titulo "Cosas veredes"
                                :referencia/ano "1999"
                                :referencia/editorial "El Bueno"
                                :referencia/ciudad "Berlin"
                                :referencia/tipo_publicacion "Libro"
                                :referencia/volumen nil
                                :referencia/nombre_libro nil
                                :referencia/nombre_revista nil })
  
  (consulta :crear-cita! {:citas/usuario #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b"
                          :citas/referencia #uuid "966e1a6a-b667-4fb2-bd8b-56372ff99809"
                          :citas/paginas "12-24"
                          :citas/cita "Esta es una cita muy interesante"})
  
  (consulta :crear-comentario! {:comentarios/comentario "Este es un comentario muy concienzudo"
                                :comentarios/paginas "12-34"
                                :comentarios/palabras_clave "a b c d e"
                                :comentarios/referencia #uuid "966e1a6a-b667-4fb2-bd8b-56372ff99809"
                                :comentarios/usuario #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b"})
  
  (consulta :crear-coleccion! {:colecciones/nombre_coll "Buenas lecturas"})
  
  (consulta :crear-item-coleccion! {:coleccion_items/coleccion #uuid "20a13609-669c-4179-9429-f54f1a571b06"
                                    :coleccion_items/referencia #uuid "966e1a6a-b667-4fb2-bd8b-56372ff99809"})
  
  (consulta :crear-biblioteca! {:bibliotecas/nombre_biblioteca "Gran biblioteca"
                                :bibliotecas/usuario #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b"})
  
  (consulta :crear-item-biblioteca! {:biblioteca_items/biblioteca #uuid "1ff6d88e-0c1c-4b0f-873a-446717d7fbc9"
                                     :biblioteca_items/coleccion #uuid "20a13609-669c-4179-9429-f54f1a571b06"})
  
  (consulta :obtener-colecciones-por-usuario {:bibliotecas/usuario #uuid "22c4d71b-42f1-46d7-8dfd-66ca4e0ce28b"
                                              :bibliotecas/id #uuid "1ff6d88e-0c1c-4b0f-873a-446717d7fbc9"}) ;;revisar
  
  (consulta :obtener-todo {:table "colecciones"})
  
  (consulta :obtener-todo {:table "coleccion_items"})
  
  (consulta :obtener-todo {:table "bibliotecas"})
  
  (consulta :obtener-referencias-por-coleccion {:colecciones/nombre_coll "Buenas lecturas"}) ;;revisar
  
  (consulta :obtener-citas-por-referencia-id {:citas/referencia #uuid "966e1a6a-b667-4fb2-bd8b-56372ff99809"}) ;; revisar
   
  :bibliotecas/colecciones #uuid "20a13609-669c-4179-9429-f54f1a571b06"
  
  (consulta :obtener-todo {:table "citas"})
  
  )
