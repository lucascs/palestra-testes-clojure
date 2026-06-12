(ns palestra-testes-clojure.components.pagamentos
  "Componente de negócio: para autorizar um pagamento, consulta o
   autorizador via http-client e, se aprovado, publica um evento via
   message-client. Depende de ambos via component/using."
  (:require [com.stuartsierra.component :as component]
            [palestra-testes-clojure.components.http-client    :as http]
            [palestra-testes-clojure.components.message-client :as msg]))

(defrecord Pagamentos [http-client message-client]
  component/Lifecycle
  (start [this] this)
  (stop  [this] this))

(defn novo []
  (map->Pagamentos {}))

(defn autoriza
  "Tenta autorizar um pagamento. Publica `:pagamentos/autorizado`
   ou `:pagamentos/negado` dependendo da resposta da autorizadora."
  [{:keys [http-client message-client]} {:keys [pagamento-id valor] :as cmd}]
  (let [{:keys [status]} (http/GET http-client (str "/autorizadora/" pagamento-id))
        autorizado?      (= 200 status)
        topic            (if autorizado? :pagamentos/autorizado :pagamentos/negado)]
    (msg/publish message-client topic
                 {:pagamento-id pagamento-id :valor valor})
    {:pagamento-id pagamento-id
     :autorizado?  autorizado?}))
