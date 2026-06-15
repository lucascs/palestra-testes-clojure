(ns palestra-testes-clojure.controller.pagamentos
  "Controller de pagamentos: orquestra o diplomat.autorizador.
   É um componente Sierra, depende de :http-client e :message-client —
   esses são repassados pro diplomat."
  (:require [com.stuartsierra.component :as component]
            [palestra-testes-clojure.diplomat.autorizador :as autorizador]))

(defrecord Pagamentos [http-client message-client]
  component/Lifecycle
  (start [this] this)
  (stop  [this] this))

(defn novo []
  (map->Pagamentos {}))

(defn autoriza
  "Tenta autorizar um pagamento. Publica `:pagamentos/autorizado`
   ou `:pagamentos/negado` dependendo da resposta da autorizadora."
  [{:keys [http-client message-client]} {:keys [pagamento-id valor]}]
  (let [resultado (autorizador/consulta-autorizacao http-client pagamento-id)]
    (autorizador/publica-resultado! message-client resultado
                                    {:pagamento-id pagamento-id :valor valor})
    {:pagamento-id pagamento-id
     :autorizado?  (= :pagamentos/autorizado resultado)}))
