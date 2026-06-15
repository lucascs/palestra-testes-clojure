(ns palestra-testes-clojure.camada6.pagamentos-test
  "Camada 6: state-flow onde o estado é um system-map de Sierra
   Components iniciado. :init = (component/start (test-system ...)),
   :cleanup = component/stop."
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
  {:init    (com-respostas {[:get "/autorizadora/123"] {:status 200}})
   :cleanup component/stop}

  (flow "autoriza retorna {:autorizado? true} e bate na autorizadora"
    [resposta (autoriza! {:pagamento-id 123 :valor 100M})]
    (match? {:pagamento-id 123 :autorizado? true} resposta)

    [chamadas (chamadas-http)]
    (match? [{:method :get :url "/autorizadora/123"}] chamadas))

  (flow "publica evento :pagamentos/autorizado"
    [msgs (mensagens-publicadas)]
    (match? [{:topic   :pagamentos/autorizado
              :message {:pagamento-id 123 :valor 100M}}]
            msgs)))

(defflow autoriza-pagamento-negado
  {:init    (com-respostas {[:get "/autorizadora/999"] {:status 403}})
   :cleanup component/stop}

  (flow "autoriza retorna {:autorizado? false}"
    [resposta (autoriza! {:pagamento-id 999 :valor 50M})]
    (match? {:pagamento-id 999 :autorizado? false} resposta))

  (flow "publica evento :pagamentos/negado, não :autorizado"
    [msgs (mensagens-publicadas)]
    (match? (m/equals [{:topic   :pagamentos/negado
                        :message {:pagamento-id 999 :valor 50M}}])
            msgs)))

(comment
  ;; Para inspecionar o sistema vivo no REPL:
  (def sys (component/start (system/test-system
                              {[:get "/autorizadora/1"] {:status 200}})))
  (pagamentos/autoriza (:pagamentos sys) {:pagamento-id 1 :valor 10M})
  @(:chamadas   (:http-client    sys))
  @(:publicadas (:message-client sys))
  (component/stop sys)
  )
