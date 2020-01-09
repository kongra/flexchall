(ns ^:figwheel-hooks cljc.flexchall.core
  #?(:clj
     (:require
      [clojure.spec.alpha     :as    spec]
      [taoensso.timbre        :as     log]
      [cljc.flexchall.service :as service]))

  #?(:clj
     (:gen-class))

  #?(:cljs
     (:require
      [reagent.core        :as    r]
      [cljc.flexchall.page :as page])))

#?(:clj (set! *warn-on-reflection* true))

;; LET'S WATCH SOME OF THE COMPILATION FLAGS
#?(:clj
   (println ":clj *assert*" *assert*))

#?(:clj
   (println ":clj spec/*compile-asserts*" spec/*compile-asserts*))

#?(:clj
   (println ":clj clojure.compiler.direct-linking"
     (System/getProperty "clojure.compiler.direct-linking")))

;; LOGGING CONF.
#?(:clj
   (defn- system-out-appender
     [{:keys [output_]}]
     (.. System out (println (force output_))) nil))

#?(:clj
   (log/set-level! :debug))

#?(:clj
   (log/set-config!
     {:level :debug
      :ns-whitelist  []
      :ns-blacklist  []
      :middleware    []

      :timestamp-opts log/default-timestamp-opts
      :output-fn      log/default-output-fn

      :appenders {:system-out-appender
                  {:enabled?   true
                   :async?     false
                   :min-level  nil
                   :rate-limit [[1 250] [10 5000]] ; 1/250ms, 10/5s
                   :output-fn  :inherit
                   :fn
                   system-out-appender}}}))

;; STANDALONE STARTUP
#?(:clj
   (defn -main
     []
     (.. Runtime getRuntime
       (addShutdownHook
         (Thread. #(do (log/debug "Shutting down")
                       (shutdown-agents)))))

     (service/start-blocking!)))

;; INSTRUMENTATION
#?(:cljs
   (defn- mount-app
     []
     (r/render-component [page/html-app]
       (-> js/document (.getElementById "dom-app")))))

#?(:cljs
   (defn ^:after-load onReload
     []
     (mount-app)))

#?(:cljs
   (enable-console-print!))

#?(:cljs
   (mount-app))
