(ns cljc.flexchall.service
  #?(:clj
     (:require
      ;; SPEC
      [clojure.spec.alpha    :as     spec]

      ;; LIBS
      [compojure.route       :as    route]
      [ring.adapter.jetty    :as    jetty]
      [ring.util.response    :as     resp]
      [taoensso.timbre       :as      log]
      [cheshire.core         :as cheshire]

      [cljc.flexchall.string :as     flex]
      [cljc.flexchall.page   :as     page]

      [ring.middleware.params
       :refer [wrap-params]]

      [ring.middleware.ssl
       :refer [wrap-hsts
               wrap-ssl-redirect
               wrap-forwarded-scheme]]

      [compojure.core
       :refer [defroutes GET]])))

#?(:clj (set! *warn-on-reflection* true))

;; ROUTING
#?(:clj
   (def ^:private NOT-FOUND-404
     (route/not-found "404 Not Found!")))

#?(:clj (declare scramble-handler))
#?(:clj
   (defroutes ROUTES
     (GET  "/"        []    page/html-app)
     (GET "/scramble" [] scramble-handler)

     ;; Public static resources
     (route/resources "/js/"  {:root "/public/js/" })
     (route/resources "/img"  {:root "/public/img/"})
     (route/resources "/css/" {:root "/public/css/"})

     ;; Figwheel stuff
     (or (when (= "true" (System/getProperty "cljc.flexchall.repl"))
           (log/debug "Figwheel will be served at /fig")
           (GET "/fig" [] page/html-fig))

       NOT-FOUND-404)

     (or (when (= "true" (System/getProperty "cljc.flexchall.repl"))
           (route/resources
             "/cljs-out/" {:root "/public/cljs-out/"}))

       NOT-FOUND-404)

     ;; Else:
     NOT-FOUND-404))

;; CONF.
#?(:clj
   (def HANDLER
     (let [h (-> ROUTES
               wrap-params)

           h (if (not= "true" (System/getProperty "cljc.flexchall.repl"))
               (do #_(log/debug "Jetty will send HSTS header(s)")
                   (wrap-hsts h))
               h)]
       (-> h
         wrap-ssl-redirect
         wrap-forwarded-scheme ;; Some clouds add x-forwarded-proto
         ))))

#?(:clj
   (def ^:private PORT
     (Long/parseLong (System/getProperty "cljc.flexchall.service.port"))))

#?(:clj
   (def ^:private SSL-KEYSTORE "scripts/ssl/flexchallpkcs12.keystore"))

#?(:clj
   (def ^:private SSL-PASSWD "flexchall12345"))

#?(:clj
   (def ^:private SSL-PORT
     (when (= "true"   (System/getProperty "cljc.flexchall.service.ssl?"))
       (Long/parseLong (System/getProperty "cljc.flexchall.service.ssl.port")))))

#?(:clj
   (defn- with-ssl
     [conf]
     (if (= "true" (System/getProperty "cljc.flexchall.service.ssl?"))
       (do (log/debug "Jetty configured to use SSL at" SSL-PORT)
           (assoc conf
             :ssl?         true
             :ssl-port     SSL-PORT
             :keystore     SSL-KEYSTORE
             :key-password SSL-PASSWD))
       conf)))

#?(:clj (def ^:private CONF-BLOCKING     (-> {:port PORT :join?  true} with-ssl)))
#?(:clj (def ^:private CONF-NON-BLOCKING (-> {:port PORT :join? false} with-ssl)))

;; NON-BLOCKING MODE
#?(:clj
   (defonce server
     (agent nil
       :error-mode    :continue
       :error-handler (fn [_ e] (log/error e)))))

#?(:clj
   (defn start-jetty!
     [s]
     (if-not s
       (do (log/debug "Starting non-blocking Jetty at" PORT "...")
           (let [s (jetty/run-jetty HANDLER CONF-NON-BLOCKING)]
             (try (log/debug "Jetty started") (catch Throwable e nil))
             s))

       (do (log/debug "Jetty already started")
           s))))

#?(:clj
   (defn- stop-jetty!
     [s]
     (if s
       (do (log/debug "Stopping Jetty ...")
           (.stop ^org.eclipse.jetty.server.Server s)
           (try (log/debug "Jetty stopped") (catch Throwable e nil)))

       (log/debug "Jetty already stopped"))))

#?(:clj
   (defn start!
     []
     (send-off server start-jetty!)))

#?(:clj
   (defn stop!
     []
     (send-off server stop-jetty!)))

#?(:clj
   (defn restart!
     []
     (stop!)
     (start!)))

;; BLOCKING-MODE
#?(:clj
   (defn start-blocking!
     []
     (do (log/debug (str "Starting blocking Jetty at " PORT))
         (jetty/run-jetty HANDLER CONF-BLOCKING))))

;; CONVENIENT RESTARTS OF NON-BLOCKING WHEN REPL
#?(:clj
   (defn port-used?
     [host port]
     (try
       (.close (java.net.Socket. ^String host (int port)))
       true

       (catch java.net.SocketException e
         false))))

#?(:clj
   (defn nrepl?
     []
     (port-used? "127.0.0.1" 7888)))

#?(:clj
   (when (and (= "true" (System/getProperty "cljc.flexchall.repl")) (nrepl?))
     (restart!)))

;; SCRAMBLE END-POINT
#?(:clj
   (defn scramble-handler
     [request]
     (let [s       (-> request (get-in [:query-params       "s"]) str .trim)
           pattern (-> request (get-in [:query-params "pattern"]) str .trim)

           json-content
           {"Content-Type" "application/json"}]

       (cond
         (not (spec/valid? :cljc.flexchall.string/scramble-string s))
         {:status  400
          :headers json-content
          :body   (cheshire/encode {:error "Bad s"})}

         (not (spec/valid? :cljc.flexchall.string/scramble-string pattern))
         {:status  400
          :headers json-content
          :body   (cheshire/encode {:error "Bad pattern"})}

         :else
         {:status  200
          :headers json-content
          :body   (cheshire/encode
                    {:scramble? (flex/scramble? s pattern)})}))))
