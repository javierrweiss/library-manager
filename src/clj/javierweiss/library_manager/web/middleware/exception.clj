(ns javierweiss.library-manager.web.middleware.exception
  (:require
    [clojure.tools.logging :as log]
    [reitit.ring.middleware.exception :as exception]
    [javierweiss.library-manager.web.views.errores :refer [error_servidor bad_request no_encontrado no_autorizado prohibido]]))


(defn handler
  ([html-response message status exception request]
   (when (>= status 500)
    ;; You can optionally use this to report error to an external service
     (log/error {:message   message
                 :status status
                 :exception (.getClass exception)
                 :data      (ex-data exception)
                 :uri       (:uri request)}))
   {:status 200
    :body html-response})
  ([message status exception request]
   {:status 200
    :body {:message   message
           :status status
           :exception (.getClass exception)
           :data      (ex-data exception)
           :uri       (:uri request)}}))

(def wrap-exception
  (exception/create-exception-middleware
    (merge
      exception/default-handlers
      {:system.exception/internal     (partial handler error_servidor "internal exception" 500)
       :system.exception/business     (partial handler bad_request "bad request" 400)
       :system.exception/not-found    (partial handler no_encontrado "not found" 404)
       :system.exception/unauthorized (partial handler no_autorizado "unauthorized" 401)
       :system.exception/forbidden    (partial handler prohibido "forbidden" 403)

       ;; override the default handler
       ::exception/default            (partial handler "default" 500)

       ;; print stack-traces for all exceptions
       ::exception/wrap               (fn [handler e request]
                                        (handler e request))})))
