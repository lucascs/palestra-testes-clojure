(ns palestra-testes-clojure.camada4.conta-test
  "Camada 4: as mesmas funções da Camada 3, agora com schemas Plumatic
   sendo validadas em runtime via use-fixtures + s/with-fn-validation."
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [matcher-combinators.matchers :as m]
            [matcher-combinators.test :refer [match?]]
            [schema.core :as s]
            [palestra-testes-clojure.logic.conta :as conta]))

(use-fixtures :each
  (fn [t]
    (s/with-fn-validation
      (t))))

(deftest fluxo-feliz
  (let [extrato (-> (conta/nova "Lucas")
                    (conta/deposita 100M)
                    (conta/deposita 50M)
                    (conta/saca    30M)
                    conta/extrato)]
    (testing "extrato bate"
      (is (match? {:conta/titular "Lucas"
                   :conta/saldo   120M}
                  extrato)))
    (testing "movimentos têm a ordem esperada"
      (is (match? {:conta/movimentos
                   (m/equals
                     [{:movimento/tipo :deposito :movimento/valor 100M}
                      {:movimento/tipo :deposito :movimento/valor 50M}
                      {:movimento/tipo :saque    :movimento/valor 30M}])}
                  extrato)))))

(deftest schema-rejeita-titular-vazio
  (testing "constrained sobre s/Str dispara antes de criar a conta"
    (is (thrown-with-msg? Exception #"titular"
          (conta/nova "")))))

(deftest schema-rejeita-valor-nao-positivo
  (testing "Valor é (s/constrained BigDecimal pos?), zero falha"
    (is (thrown-with-msg? Exception #"positivo|Value does not match schema"
          (conta/deposita (conta/nova "Lucas") 0M))))
  (testing "negativo também falha"
    (is (thrown-with-msg? Exception #"positivo|Value does not match schema"
          (conta/deposita (conta/nova "Lucas") -1M)))))

(deftest saque-sem-saldo-mantem-ex-info
  (testing "regra de negócio ainda lança, e o payload usa chaves namespaceadas"
    (is (thrown-match? clojure.lang.ExceptionInfo
                       {:conta/saldo 0M :movimento/valor 10M}
                       (conta/saca (conta/nova "Lucas") 10M)))))
