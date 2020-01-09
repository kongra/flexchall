(defproject flexchall "0.1.0"
  :description      "Fexiana Challenge App"
  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure           "1.10.0"]
                 [org.clojure/clojurescript   "1.10.597"]
                 [org.clojure/core.async       "0.6.532"]
                 [org.clojure/test.check        "0.10.0"]

                 [com.taoensso/timbre           "4.10.0"]
                 [org.slf4j/slf4j-simple        "1.7.30"]

                 [reagent                        "0.8.1"]
                 [hiccup                         "1.0.5"]
                 [cljs-http                     "0.1.46"]
                 [ring/ring-core                 "1.8.0"]
                 [ring/ring-jetty-adapter        "1.8.0"]
                 [ring/ring-ssl                  "0.3.0"]
                 [ring/ring-session-timeout      "0.2.0"]
                 [ring-cors                     "0.1.13"]
                 [compojure                      "1.6.1"]

                 [cheshire                       "5.9.0"]

                 [kongra/ch                     "0.1.26"]]

  :plugins      [[lein-cljsbuild                 "1.1.7"]]

  :source-paths ["src/main/cljc"]
  :test-paths   ["test/clojure"]

  :aot          :all
  ;; :pedantic? :warn
  :global-vars  {*warn-on-reflection* false
                 *assert*             false
                 *print-length*         500}

  :jvm-opts ["-Dcljc.flexchall.base-url=https://localhost:8443"

             "-Dcljc.flexchall.service.port=8080"
             "-Dcljc.flexchall.service.ssl?=true"
             "-Dcljc.flexchall.service.ssl.port=8443"]

  :clean-targets ^{:protect false} ["target"]

  :aliases {"fig:repl" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]}

  :profiles {:uberjar {:jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.spec.compile-asserts=false"
                                  "-Dclojure.spec.check-asserts=false"]}

             :repl {:plugins    [[cider/cider-nrepl "0.23.0-SNAPSHOT"]]
                    :middleware  [cider-nrepl.plugin/middleware       ]

                    :global-vars {*assert* true}

                    :jvm-opts ["-Dclojure.compiler.direct-linking=false"
                               "-Dclojure.spec.compile-asserts=true"
                               "-Dclojure.spec.check-asserts=true"
                               "-Dcljc.flexchall.repl=true"
                               "-XX:-OmitStackTraceInFastThrow"
                               "-server"
                               "-Xms512m"
                               "-Xmx512m"
                               "-XX:+UseStringDeduplication"
                               "-XX:+DoEscapeAnalysis"
                               "-XX:+UseCompressedOops"
                               ;; "-verbose:gc"
                               ]

                    :source-paths ^:replace ["src/main/cljc"]}

             :dev  {:dependencies  [[com.bhauman/figwheel-main       "0.2.3" ]
                                    [com.bhauman/rebel-readline-cljs "0.1.4" ]]

                    :global-vars {*assert* true}
                    :jvm-opts    ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.spec.compile-asserts=true"
                                  "-Dclojure.spec.check-asserts=true"
                                  "-XX:-OmitStackTraceInFastThrow"
                                  "-server"
                                  "-Xms512m"
                                  "-Xmx512m"
                                  "-XX:+UseStringDeduplication"
                                  "-XX:+DoEscapeAnalysis"
                                  "-XX:+UseCompressedOops"]

                    :source-paths ^:replace ["src/main/cljc"]

                    :resource-paths ["target"]}}
  :cljsbuild
  {:builds
   [{:id "flexchall-min"
     :source-paths ^:replace ["src/main/cljc"]

     :compiler {:output-to       "resources/public/js/flexchall.js"
                :main             cljc.flexchall.core
                :optimizations    :advanced
                :static-fns       true
                :fn-invoke-direct true
                :pretty-print     false
                :elide-asserts    true}}]})
