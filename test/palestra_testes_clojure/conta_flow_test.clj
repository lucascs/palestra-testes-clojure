(ns palestra-testes-clojure.conta-flow-test
  "Camada 4: testes em estilo state-flow, encadeando passos sobre um
   'mundo' (mapa de contas) e fazendo asserts com matcher-combinators."
  (:require [clojure.test :refer [deftest]]
            [matcher-combinators.matchers :as m]
            [state-flow.api :refer [defflow flow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as flow-core]
            [state-flow.state :as state]
            [palestra-testes-clojure.conta :as conta]))

(defn- abre-conta! [titular]
  (state/modify update :contas assoc titular (conta/nova-conta titular)))

(defn- deposita! [titular valor]
  (state/modify update-in [:contas titular] conta/deposita valor))

(defn- saca! [titular valor]
  (state/modify update-in [:contas titular] conta/saca valor))

(defn- extrato-de [titular]
  (state/gets #(conta/extrato (get-in % [:contas titular]))))

(def estado-inicial {:contas {}})

(defflow fluxo-feliz-de-uma-conta
  {:init (constantly estado-inicial)}
  (flow "abrindo conta e movimentando"
    (abre-conta! "Lucas")
    (deposita! "Lucas" 100M)
    (deposita! "Lucas" 50M)
    (saca!     "Lucas" 30M)

    (flow "extrato bate com o esperado"
      [extrato (extrato-de "Lucas")]
      (match? {:titular    "Lucas"
               :saldo      120M
               :movimentos (m/embeds [{:tipo :saque :valor 30M}])}
              extrato))))

(defflow varias-contas-sao-independentes
  {:init (constantly estado-inicial)}
  (flow "duas contas convivem sem interferência"
    (abre-conta! "Lucas")
    (abre-conta! "Ana")
    (deposita! "Lucas" 100M)
    (deposita! "Ana"    10M)

    [lucas (extrato-de "Lucas")
     ana   (extrato-de "Ana")]
    (match? 100M (:saldo lucas))
    (match?  10M (:saldo ana))))

(comment
  ;; Para rodar um flow direto no REPL sem clojure.test:
  (flow-core/run* {:init (constantly estado-inicial)}
                  fluxo-feliz-de-uma-conta))
