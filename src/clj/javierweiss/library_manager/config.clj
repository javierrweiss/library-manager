(ns javierweiss.library-manager.config
  (:require
    [kit.config :as config]))

(def ^:const system-filename "system.edn")

(def ^:const custom-config-filename "config.edn")

(defn system-config
  [options]
  (config/read-config system-filename options))

(def custom-config (config/read-config custom-config-filename {}))
