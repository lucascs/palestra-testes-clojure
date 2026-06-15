(ns palestra-testes-clojure.core-property-test
  "Camada 2: testes generativos de propriedade com test.check."
  (:require [clojure.test :refer [deftest]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as properties]
            [palestra-testes-clojure.core :as core]))

(defspec multiplos-de-dois-sao-sempre-pares
  100
  (properties/for-all [n gen/small-integer]
    (core/eh-par? (* 2 n))))

(defspec multiplos-de-dois-mais-um-sao-sempre-impares
  100
  (properties/for-all [n gen/small-integer]
    (not (core/eh-par? (+ 1 (* 2 n))))))

(defspec soma-pares-eh-sempre-par
  100
  (properties/for-all [xs (gen/vector gen/small-integer)]
    (core/eh-par? (core/soma-pares xs))))

(defspec soma-pares-ignora-impares
  100
  (properties/for-all [pares   (gen/vector (gen/fmap #(* 2 %) gen/small-integer))
                       impares (gen/vector (gen/fmap #(inc (* 2 %)) gen/small-integer))]
    (= (core/soma-pares pares)
       (core/soma-pares (concat pares impares)))))
