(ns palestra-testes-clojure.camada2.core-test
  "Camada 2: testes generativos de propriedade com test.check.
   Reaproveita as mesmas funções de palestra-testes-clojure.logic.math."
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as properties]
            [palestra-testes-clojure.logic.math :as math]))

(defspec multiplos-de-dois-sao-sempre-pares
  100
  (properties/for-all [n gen/small-integer]
    (math/eh-par? (* 2 n))))

(defspec multiplos-de-dois-mais-um-sao-sempre-impares
  100
  (properties/for-all [n gen/small-integer]
    (not (math/eh-par? (+ 1 (* 2 n))))))

(defspec soma-pares-eh-sempre-par
  100
  (properties/for-all [xs (gen/vector gen/small-integer)]
    (math/eh-par? (math/soma-pares xs))))

(defspec soma-pares-ignora-impares
  100
  (properties/for-all [pares   (gen/vector (gen/fmap #(* 2 %) gen/small-integer))
                       impares (gen/vector (gen/fmap #(inc (* 2 %)) gen/small-integer))]
    (= (math/soma-pares pares)
       (math/soma-pares (concat pares impares)))))
