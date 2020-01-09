(ns ^:figwheel-hooks cljc.flexchall.page
  #?(:clj
     (:require
      [hiccup.page :refer [html5]]))

  #?(:cljs
     (:require
      ;; SPEC
      [cljs.spec.alpha               :as            spec]
      [cljs.kongra.spec.alpha.macros :refer-macros [specInstr specCheck specAl]]
      [cljs.spec.test.alpha          :refer-macros [instrument instrument-1   ]]
      [cljs.spec.test.alpha          :refer        [instrument-1*             ]]
      [clojure.test.check]
      [clojure.test.check.properties]

      ;; LIBS
      [cljs.core.async  :as    a]
      [reagent.core     :as    r]
      [cljs-http.client :as http]))

  #?(:cljs
     (:require-macros
      [cljs.core.async :refer [go]])))

#?(:clj (set! *warn-on-reflection* true))

;; LET'S WATCH SOME THE COMPILATION FLAGS (ClojureScript)
#?(:cljs (spec/check-asserts true))

#?(:cljs
   (println ":cljs spec/*compile-asserts*" spec/*compile-asserts*))

#?(:cljs
   (println ":cljs (spec/check-asserts?)" (spec/check-asserts?)))

;; BACK-END HTML
#?(:clj
   (defn html-app-template
     ([request]
      (html-app-template request nil))

     ([request {:keys [fig?]}]
      (html5
        [:html {:lang "en"}
         [:head
          [:meta {:charset "utf-8"}]
          [:meta {:name    "viewport"
                  :content "user-scalable=no,
                          initial-scale=1,
                          maximum-scale=1,
                          minimum-scale=1,
                          width=device-width,
                          height=device-height"}]

          [:meta {:name "apple-mobile-web-app-capable" :content "yes"     }]
          [:meta {:name "mobile-web-app-capable"       :content "yes"     }]
          [:meta {:http-equiv "x-ua-compatible"        :content "ie=edge" }]
          [:meta {:http-equiv "Cache-Control"
                  :content "no-cache,no-store,must-revalidate"            }]

          [:meta {:http-equiv "Pragma"                 :content "no-cache"}]
          [:meta {:http-equiv "Expires"                :content "0"       }]]

         [:body
          [:div#dom-app [:div "Loading..."]]
          [:script
           {:src (if-not fig?
                   "/js/flexchall.js"
                   "/js/cljs-out/dev-main.js")}]]]))))

#?(:clj
   (defn html-app
     [request]
     (html-app-template request)))

#?(:clj
   (defn html-fig
     [request]
     (html-app-template request {:fig? true})))

;; APP
#?(:cljs (def ^:private s-atom       (r/atom "")))
#?(:cljs (def ^:private pattern-atom (r/atom "")))

#?(:cljs (def ^:private message-atom
           ;; The (above) initial empty strings always match
           (r/atom "Strings matching")))

#?(:cljs (declare on-input-key-up!))
#?(:cljs
   (defn html-app
     []
     [:div.dom-app
      {:style {:display :grid
               :gap "10px"
               :grid-template-columns "180px 200px"
               :grid-template-rows    "30px  30px"}}

      [:div {:style {:align-self :center}}
       "String to match (s)"]

      [:input#dom-s
       {:type "text"
        :on-key-up #(on-input-key-up! s-atom "dom-s")}]

      [:div {:style {:align-self :center}}
       "Pattern"]

      [:input#dom-pattern
       {:type "text"
        :on-key-up #(on-input-key-up! pattern-atom "dom-pattern")}]

      [:div
       {:style {:align-self   :center
                :justify-self :right
                :grid-column  "1/3"
                :font-weight  600}}

       @message-atom]]))

#?(:cljs (declare hit-scramble!))
#?(:cljs
   (defn- on-input-key-up!
     [a id]
     (let [old-value @a
           value (-> js/document (.getElementById id) .-value)]

       (when-not (= value old-value)
         (reset! a value)
         (go (<! (a/timeout 1000))
             (let [value1 (-> js/document (.getElementById id) .-value)]
               (when (= value1 value)
                 ;; Nobody has changed me in this 1s
                 (hit-scramble!))))))))

#?(:cljs
   (defn- hit-scramble!
     []
     (go
       (let [resp
             (<! (http/get "/scramble"
                   {:with-credentials? false
                    :query-params {"s"       @s-atom
                                   "pattern" @pattern-atom}}))]

         (if (:success resp)
           (reset! message-atom
             (if (-> resp :body :scramble?)
               "Strings matching"
               "Strings not matching"))

           (reset! message-atom
             (-> resp :body :error)))))))
