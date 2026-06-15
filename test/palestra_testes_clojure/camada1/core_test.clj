(ns palestra-testes-clojure.core-test
  "Camada 1: testes de unidade com clojure.test."
  (:require [clojure.test :refer [deftest is testing]]
            [palestra-testes-clojure.core :as core]))

(deftest eh-par-test
  (testing "checa se o número é par"
    (is (core/eh-par? 2))
    (is (core/eh-par? 0))
    (is (core/eh-par? -4))
    (is (not (core/eh-par? 3)))))

(deftest eh-impar-test
  (testing "checa se o número é ímpar"
    (is (core/eh-impar? 1))
    (is (not (core/eh-impar? 2)))))

(deftest soma-pares-test
  (testing "soma só os pares da lista"
    (is (= 6 (core/soma-pares [1 2 3 4])))
    (is (= 0 (core/soma-pares [1 3 5])))
    (is (= 0 (core/soma-pares [])))))
