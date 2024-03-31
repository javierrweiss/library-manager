(ns javierweiss.library-manager.web.security.adminconfig
  (:require [taoensso.tempel :as tempel]
            [javierweiss.library-manager.db.db :as db]
            [javierweiss.library-manager.config :refer [custom-config]]
            #_[javierweiss.library-manager.core :refer [system]]
            [integrant.repl.state :as state]))

(def dbtype (:db/type state/system #_system))

(def state-map
  (if (= dbtype :xtdb)
    {:db-type dbtype
     :query-fn (:db.xtdb/node state/system #_system)}
    {:db-type dbtype
     :query-fn (:db.sql/query-fn state/system #_system)}))

;;Devuelve id de administrador
(defonce usuario_admin
  (db/crear-usuario state-map
                    "Administrador"
                    (-> custom-config :credentials :mail)
                    "Admin"
                    (tempel/keychain-encrypt (tempel/keychain)
                                             {:pbkdf-nwf :ref-2000-msecs
                                              :password  (-> custom-config :credentials :key)})))

(defonce admin-public-key
  (if (= dbtype :xtdb)
    (->> usuario_admin
         (db/obtener-usuario-por-id state-map)
         ffirst 
         :usuario/clave 
         tempel/public-data 
         :keychain)
    (->> usuario_admin
         first
         :id
         (db/obtener-usuario-por-id state-map)
         :clave
         tempel/public-data
         :keychain)))
 
(comment

  (db/obtener-usuario-por-id state-map #uuid "fdd2401c-b860-4a0b-bc87-cb3b0b490a90")

  (db/obtener-usuario-por-id {:db-type :sql
                              :query-fn (:db.sql/query-fn state/system #_system)}
                             #uuid "c945f2f9-ec5d-4194-8201-ed6cec75a277")

  (defonce sql-admin (db/crear-usuario {:db-type :sql
                                        :query-fn (:db.sql/query-fn state/system #_system)}
                                       "Administrador"
                                       (-> custom-config :credentials :mail)
                                       "Admin"
                                       (tempel/keychain-encrypt (tempel/keychain)
                                                                {:pbkdf-nwf :ref-2000-msecs
                                                                 :password  (-> custom-config :credentials :key)})))
  (-> sql-admin first :id)

  (when-let [adm (db/obtener-usuario-por-id {:db-type :sql
                                             :query-fn (:db.sql/query-fn state/system #_system)}
                                            #uuid "c945f2f9-ec5d-4194-8201-ed6cec75a277")]
    (-> (:clave adm) tempel/public-data :keychain))

  (when-let [adm (db/obtener-usuario-por-id {:db-type :sql
                                             :query-fn (:db.sql/query-fn state/system #_system)}
                                            #uuid "1245-ec5d-4194-8201-ed6cec75a277")]
    (-> (:clave adm) tempel/public-data :keychain))

  (defn pk-test
    [state-map dbtype usuario_admin]
    (if (= dbtype :xtdb)
      (->> usuario_admin
           (db/obtener-usuario-por-id state-map)
           ffirst
           :usuario/clave
           tempel/public-data
           :keychain)
      (->> usuario_admin
           first
           :id
           (db/obtener-usuario-por-id state-map)
           :clave
           tempel/public-data
           :keychain)))
  
  (pk-test {:db-type :sql
            :query-fn (:db.sql/query-fn state/system)}
           :sql
           [{:id #uuid "c945f2f9-ec5d-4194-8201-ed6cec75a277"}])
  
  (pk-test {:db-type :xtdb
            :query-fn (:db.xtdb/node state/system)}
           :xtdb
           #uuid "2a774126-1e85-4bd6-8989-39fac3ff5a64")

  :rcf)