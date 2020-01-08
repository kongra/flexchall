(ns test.cljc.flexchall.string
  (:require
   [clojure.test    :refer :all]
   [clojure.spec.alpha :as spec]

   [cljc.flexchall.string :refer [scramble?]]))

(println "clj: *assert*" *assert*)
(println "clj:  spec/*compile-asserts*" spec/*compile-asserts*)
(println "clj: (spec/check-asserts?)"  (spec/check-asserts?))

(defn shuffle-str
  [s]
  (->> s
    (seq)
    (shuffle)
    (apply str)))

(defn gen-scramble
  [s noise]
  (shuffle-str (str s noise)))

(defn gen-non-scramble
  [s noise]
  ;; Just dropping 1-st letter is enough to break things
  (shuffle-str (str (.substring s 1) noise)))

(deftest test-scramble
  (is (true?   (scramble? "rekqodlw"             "world")))
  (is (true?   (scramble? "cedewaraaossoqqyt" "codewars")))
  (is (false?  (scramble? "katas"                "steak")))

  (is (true?   (scramble? "abcd"     "")))
  (is (true?   (scramble? "abcd" "abcd")))
  (is (true?   (scramble? "abcd" "cbda")))
  (is (true?   (scramble? "abcd"  "bcd")))
  (is (true?   (scramble? "abcd"  "cbd")))

  (is (false?  (scramble? "abc"  "abcd")))
  (is (false?  (scramble? "bcd"  "cbda")))

  (is (false?  (scramble? "ab"   "abcd")))
  (is (false?  (scramble? "bc"   "cbda")))

  (is (false?  (scramble? "a"    "abcd")))
  (is (false?  (scramble? "b"    "cbda")))

  (is (false?  (scramble? ""     "abcd")))
  (is (false?  (scramble? ""     "cbda")))

  ;; Some random ones
  (is (true?  (scramble? (gen-scramble     "abcd" "xyz") "abcd")))
  (is (true?  (scramble? (gen-scramble     "abcd" "xyz") "abcd")))
  (is (true?  (scramble? (gen-scramble     "abcd" "xyz") "abcd")))
  (is (true?  (scramble? (gen-scramble     "abcd" "xyz") "abcd")))
  (is (true?  (scramble? (gen-scramble     "abcd" "xyz") "abcd")))
  (is (true?  (scramble? (gen-scramble     "abcd" "xyz") "abcd")))
  (is (true?  (scramble? (gen-scramble     "abcd" "xyz") "abcd")))
  (is (true?  (scramble? (gen-scramble     "abcd" "xyz") "abcd")))
  (is (true?  (scramble? (gen-scramble     "abcd" "xyz") "abcd")))
  (is (true?  (scramble? (gen-scramble     "abcd" "xyz") "abcd")))

  (is (false? (scramble? (gen-non-scramble "abcd" "xyz") "abcd")))
  (is (false? (scramble? (gen-non-scramble "abcd" "xyz") "abcd")))
  (is (false? (scramble? (gen-non-scramble "abcd" "xyz") "abcd")))
  (is (false? (scramble? (gen-non-scramble "abcd" "xyz") "abcd")))
  (is (false? (scramble? (gen-non-scramble "abcd" "xyz") "abcd")))
  (is (false? (scramble? (gen-non-scramble "abcd" "xyz") "abcd")))
  (is (false? (scramble? (gen-non-scramble "abcd" "xyz") "abcd")))
  (is (false? (scramble? (gen-non-scramble "abcd" "xyz") "abcd")))
  (is (false? (scramble? (gen-non-scramble "abcd" "xyz") "abcd")))
  (is (false? (scramble? (gen-non-scramble "abcd" "xyz") "abcd")))

  ;; Illegal args, thrown only in tests/dev, in production setting the spec is
  ;; turned off (see specInstr, specCheck in kongra.ch)
  (is (thrown? Exception (scramble?  nil   nil)))
  (is (thrown? Exception (scramble? "abc"  nil)))
  (is (thrown? Exception (scramble?  nil  "abc")))
  (is (thrown? Exception (scramble? "abc"  "1")))
  (is (thrown? Exception (scramble? "abc"  ".")))
  (is (thrown? Exception (scramble? "abc"  " ")))
  (is (thrown? Exception (scramble? "abc"  "\n")))
  (is (thrown? Exception (scramble? "abc"  "\t"))))

;; (run-tests)
