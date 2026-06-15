(ns palestra-testes-clojure.camada3.conta-test
  "Camada 3: testes de dados com matcher-combinators sobre a logic.conta.
   Aqui as schemas de :- NÃO estão sendo validadas em runtime
   (sem with-fn-validation) — é só a função pura sendo exercitada."
  (:require [clojure.test :refer [deftest is testing]]
            [matcher-combinators.matchers :as m]
            [matcher-combinators.test :refer [match?]]
            [palestra-testes-clojure.logic.conta :as conta]))

(deftest match-parcial-vs-equals
  (let [x {:a 1 :b 2}]
    (testing "match? casa parcialmente (não exige b)"
      (is (match? {:a 1} x)))
    (testing "m/equals exige o mapa inteiro"
      (is (match? (m/equals {:a 1 :b 2}) x)))))

(deftest extrato-apos-movimentacoes
  (let [resultado (-> (conta/nova "Lucas")
                      (conta/deposita 100M)
                      (conta/deposita 50M)
                      (conta/saca 30M)
                      conta/extrato)]
    (testing "saldo e titular saem como esperado"
      (is (match? {:conta/titular "Lucas"
                   :conta/saldo   120M}
                  resultado)))
    (testing "movimentos preservam a ordem de execução"
      (is (match? {:conta/movimentos
                   [{:movimento/tipo :deposito :movimento/valor 100M}
                    {:movimento/tipo :deposito :movimento/valor 50M}
                    {:movimento/tipo :saque    :movimento/valor 30M}]}
                  resultado)))
    (testing "in-any-order quando a ordem não importa"
      (is (match? {:conta/movimentos
                   (m/in-any-order
                     [{:movimento/tipo :saque    :movimento/valor 30M}
                      {:movimento/tipo :deposito :movimento/valor 50M}
                      {:movimento/tipo :deposito :movimento/valor 100M}])}
                  resultado)))))

(deftest saque-sem-saldo-lanca
  (testing "ex-info traz contexto que o matcher consegue inspecionar"
    (is (thrown-match? clojure.lang.ExceptionInfo
                       {:conta/saldo 0M :movimento/valor 10M}
                       (conta/saca (conta/nova "Lucas") 10M)))))
