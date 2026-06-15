(ns palestra-testes-clojure.components.message-client
  "Componente de mensageria. O stub registra as mensagens publicadas
   em um atom, para o teste poder fazer assert depois."
  (:require [com.stuartsierra.component :as component]))

(defprotocol MessageClient
  (publish [this topic message]))

(defrecord StubMessageClient [publicadas]
  component/Lifecycle
  (start [this]
    (reset! publicadas [])
    this)
  (stop  [this]
    this)

  MessageClient
  (publish [_ topic message]
    (swap! publicadas conj {:topic topic :message message})))

(defn novo-stub []
  (->StubMessageClient (atom [])))
