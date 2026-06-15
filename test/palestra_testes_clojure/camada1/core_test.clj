(ns palestra-testes-clojure.camada1.core-test
  "Camada 1: testes de unidade com clojure.test."
  (:require [clojure.test :refer [deftest is testing]]
            [palestra-testes-clojure.logic.math :as math]))

(deftest eh-par-test
  (testing "checa se o número é par"
    (is (math/eh-par? 2))
    (is (math/eh-par? 0))
    (is (math/eh-par? -4))
    (is (not (math/eh-par? 3)))))

(deftest eh-impar-test
  (testing "checa se o número é ímpar"
    (is (math/eh-impar? 1))
    (is (not (math/eh-impar? 2)))))

(deftest soma-pares-test
  (testing "soma só os pares da lista"
    (is (= 6 (math/soma-pares [1 2 3 4])))
    (is (= 0 (math/soma-pares [1 3 5])))
    (is (= 0 (math/soma-pares [])))))
