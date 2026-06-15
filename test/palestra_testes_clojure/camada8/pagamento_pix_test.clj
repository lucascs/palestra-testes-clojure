(ns palestra-testes-clojure.camada8.pagamento-pix-test
  "Camada 8: testes multi-serviço. Cada serviço (conta-bancaria, pix,
   pagamentos) tem seu próprio system rodando em paralelo; o teste
   exercita uma transação que atravessa os três e verifica o efeito
   em cada um.

   Os helpers ficam em palestra-testes-clojure.camada8.dsl.* — aqui o
   teste fala em vocabulário de produto, não de detalhes de cada
   sistema."
  (:require [com.stuartsierra.component :as component]
            [state-flow.api :refer [defflow flow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [conta-bancaria.system :as conta-bancaria.system]
            [pagamentos.system     :as pagamentos.system]
            [pix.system            :as pix.system]
            [palestra-testes-clojure.camada8.dsl.conta-bancaria :as conta-bancaria]
            [palestra-testes-clojure.camada8.dsl.pagamentos     :as pagamentos]
            [palestra-testes-clojure.camada8.dsl.pix            :as pix]))

(defn- inicia-multi-services [systems]
  (fn []
    (reduce-kv (fn [acc k sys] (assoc acc k (component/start sys)))
               {} systems)))

(defn- para-multi-services [estado]
  (reduce-kv (fn [acc k sys] (assoc acc k (component/stop sys)))
             {} estado))

(def systems {:conta-bancaria (conta-bancaria.system/test-system)
              :pagamentos     (pagamentos.system/test-system)
              :pix            (pix.system/test-system)})

(defflow pagamento-do-pix-com-saldo-da-conta
  {:init    (inicia-multi-services systems)
   :cleanup para-multi-services}

  (flow "abre a conta e registra a chave"
    [conta     (conta-bancaria/cria-conta! {:titular "Lucas" :saldo 1000M})
     chave-pix (pix/chave "manda-o-pix")]
    (pagamentos/autoriza! {:origem conta :destino chave-pix :valor 300M})

    (flow "conta-bancaria debitou"
      [saldo (conta-bancaria/saldo conta)]
      (match? 700M saldo))

    (flow "pix registrou a transação"
      [transacoes (pix/transacoes)]
      (match? [{:pix/chave "manda-o-pix" :pix/valor 300M}] transacoes))))
