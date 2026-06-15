(ns palestra-testes-clojure.camada6.pagamentos-test
  "Camada 6: state-flow onde o estado é um system-map de Sierra
   Components iniciado. :init = (component/start (test-system ...)),
   :cleanup = component/stop.

   Respostas HTTP e asserts são endereçados por bookmark — não há mais
   URL nem topic literais espalhados pelo teste."
  (:require [com.stuartsierra.component :as component]
            [matcher-combinators.matchers :as m]
            [state-flow.api :refer [defflow flow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.state :as state]
            [palestra-testes-clojure.controller.pagamentos :as pagamentos]
            [palestra-testes-clojure.system                :as system]))

;; ── Helpers que falam com o sistema dentro do state-flow ───────────────────

(defn- autoriza! [cmd]
  (state/gets (fn [{:keys [pagamentos]}]
                (pagamentos/autoriza pagamentos cmd))))

(defn- chamadas-http []
  (state/gets (fn [{:keys [http-client]}] @(:chamadas http-client))))

(defn- mensagens-publicadas []
  (state/gets (fn [{:keys [message-client]}] @(:publicadas message-client))))

(defn- com-respostas [respostas]
  (fn [] (component/start (system/test-system respostas))))

;; ── Flows ──────────────────────────────────────────────────────────────────

(defflow autoriza-pagamento-aprovado
  {:init    (com-respostas {:autorizador/consulta-autorizacao {:status 200}})
   :cleanup component/stop}

  (flow "autoriza retorna {:autorizado? true} e bate na autorizadora"
    [resposta (autoriza! {:pagamento-id 123 :valor 100M})]
    (match? {:pagamento-id 123 :autorizado? true} resposta)

    [chamadas (chamadas-http)]
    (match? [{:method   :get
              :bookmark :autorizador/consulta-autorizacao
              :url      "/autorizadora/123"}]
            chamadas))

  (flow "publica evento :pagamentos/autorizado"
    [msgs (mensagens-publicadas)]
    (match? [{:bookmark :pagamentos/autorizado
              :topic    "PAGAMENTOS.AUTORIZADO"
              :payload  {:pagamento-id 123 :valor 100M}}]
            msgs)))

(defflow autoriza-pagamento-negado
  {:init    (com-respostas {:autorizador/consulta-autorizacao {:status 403}})
   :cleanup component/stop}

  (flow "autoriza retorna {:autorizado? false}"
    [resposta (autoriza! {:pagamento-id 999 :valor 50M})]
    (match? {:pagamento-id 999 :autorizado? false} resposta))

  (flow "publica evento :pagamentos/negado (m/equals fixa o shape inteiro)"
    [msgs (mensagens-publicadas)]
    (match? (m/equals [{:bookmark :pagamentos/negado
                        :topic    "PAGAMENTOS.NEGADO"
                        :payload  {:pagamento-id 999 :valor 50M}}])
            msgs)))

(comment
  ;; Para inspecionar o sistema vivo no REPL:
  (def sys (component/start (system/test-system
                              {:autorizador/consulta-autorizacao {:status 200}})))
  (pagamentos/autoriza (:pagamentos sys) {:pagamento-id 1 :valor 10M})
  @(:chamadas   (:http-client    sys))
  @(:publicadas (:message-client sys))
  (component/stop sys)
  )
