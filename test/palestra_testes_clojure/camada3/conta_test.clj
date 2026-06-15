(ns palestra-testes-clojure.conta-test
  "Camada 3: testes de dados com matcher-combinators."
  (:require [clojure.test :refer [deftest is testing]]
            [matcher-combinators.matchers :as m]
            [matcher-combinators.test :refer [match?]]
            [palestra-testes-clojure.conta :as conta]))

(deftest match-parcial-vs-equals
  (let [x {:a 1 :b 2}]
    (testing "match? casa parcialmente (não exige b)"
      (is (match? {:a 1} x)))
    (testing "m/equals exige o mapa inteiro"
      (is (match? (m/equals {:a 1 :b 2}) x)))))

(deftest extrato-apos-movimentacoes
  (let [resultado (-> (conta/nova-conta "Lucas")
                      (conta/deposita 100M)
                      (conta/deposita 50M)
                      (conta/saca 30M)
                      conta/extrato)]
    (testing "saldo e titular saem como esperado"
      (is (match? {:titular "Lucas"
                   :saldo   120M}
                  resultado)))
    (testing "movimentos preservam a ordem de execução"
      (is (match? {:movimentos [{:tipo :deposito :valor 100M}
                                {:tipo :deposito :valor 50M}
                                {:tipo :saque    :valor 30M}]}
                  resultado)))
    (testing "in-any-order quando a ordem não importa"
      (is (match? {:movimentos (m/in-any-order
                                 [{:tipo :saque    :valor 30M}
                                  {:tipo :deposito :valor 50M}
                                  {:tipo :deposito :valor 100M}])}
                  resultado)))))

(deftest saque-sem-saldo-lanca
  (testing "ex-info traz contexto que o matcher consegue inspecionar"
    (is (thrown-match? clojure.lang.ExceptionInfo
                       {:saldo 0M :valor 10M}
                       (conta/saca (conta/nova-conta "Lucas") 10M)))))
