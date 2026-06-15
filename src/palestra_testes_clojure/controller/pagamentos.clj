(ns palestra-testes-clojure.controller.pagamentos
  "Controller de pagamentos: para autorizar, consulta a autorizadora
   via diplomat.http e publica um evento via diplomat.message.
   É um componente Sierra, depende de :http-client e :message-client."
  (:require [com.stuartsierra.component :as component]
            [palestra-testes-clojure.diplomat.http    :as http]
            [palestra-testes-clojure.diplomat.message :as msg]))

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
  (let [{:keys [status]} (http/GET http-client (str "/autorizadora/" pagamento-id))
        autorizado?      (= 200 status)
        topic            (if autorizado? :pagamentos/autorizado :pagamentos/negado)]
    (msg/publish message-client topic
                 {:pagamento-id pagamento-id :valor valor})
    {:pagamento-id pagamento-id
     :autorizado?  autorizado?}))
