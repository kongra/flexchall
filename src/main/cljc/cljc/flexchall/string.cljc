(ns cljc.flexchall.string
  #?(:clj
     (:require
      ;; SPEC
      [clojure.spec.alpha            :as    spec]
      [clojure.spec.gen.alpha        :as specgen]
      [clojure.test.check.generators :as    gens]
      [cljc.kongra.spec.alpha        :refer :all]

      ;; LIBS: put below (if needed)
      )))

#?(:clj (set! *warn-on-reflection* true))

#?(:clj
   (def ^:private gen-scramble-alpha
     (gens/fmap char
       (gens/one-of
         ;;            \a..\z
         [(gens/choose 97 122)]))))

#?(:clj
   (def ^:private gen-scramble-string
     (gens/fmap clojure.string/join
       (gens/vector gen-scramble-alpha))))

#?(:clj
   (spec/def ::scramble-string
     (spec/spec
       (spec/and string? #(re-matches #"^[a-z]*$" %))

       :gen #(do gen-scramble-string))))

#?(:clj
   (spec/fdef scramble?
     :args (spec/cat
             :s       ::scramble-string
             :pattern ::scramble-string)

     :ret boolean?))

#?(:clj
   (defn scramble?
     [^String s ^String pattern]
     (loop [s s
            patseq (seq pattern)]

       (if-let [patchar (first patseq)]
         (let [s1 (.replaceFirst s (str patchar) "")]
           (if (= s1 s)
             ;; The replacement didn't change anything =>
             ;; The patchar is not present in s        =>
             ;; FAILURE
             false

             (recur s1 (rest patseq))))

         ;; No more pattern chars => all verified => SUCCESS
         true))))

#?(:clj (specInstr `scramble?))
#?(:clj (specCheck `scramble? 10000))

;; PERF. CONSIDERATIONS
;; (use 'criterium.core)

;; (quick-bench (scramble? "rekqodlw" "world"))
;;   Execution time mean : 1,822677 µs

;; (quick-bench (scramble? "cedewaraaossoqqyt" "codewars"))
;;   Execution time mean : 2,863749 µs

;; (quick-bench (scramble? "katas" "steak"))
;;   Execution time mean : 1,047012 µs

;; Comment: Looks good in std. applications. Quick perf. optimization may be
;; applied by replacing iteration across patseq with a pattern char index
;; running in 0 .. pattern.length-1
;; Also, .replaceFirst creates a String from char, so to eliminate this we
;; should maybe come up with our custom impl or use StringBuilder (if possible)
