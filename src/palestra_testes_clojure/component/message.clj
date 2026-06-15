(ns palestra-testes-clojure.component.message
  "Componente de mensageria. Recebe os topic-bookmarks na construção:
   cada bookmark mapeia uma chave de domínio (ex. `:pagamentos/autorizado`)
   para o tópico real (`\"PAGAMENTOS.AUTORIZADO\"`) e o schema do payload."
  (:require [com.stuartsierra.component :as component]))

(defprotocol MessageClient
  (publish [this bookmark payload]))

(defrecord StubMessageClient [bookmarks publicadas]
  component/Lifecycle
  (start [this]
    (reset! publicadas [])
    this)
  (stop  [this]
    this)

  MessageClient
  (publish [_ bookmark payload]
    (let [{:keys [topic]} (get bookmarks bookmark)]
      (swap! publicadas conj {:bookmark bookmark
                              :topic    topic
                              :payload  payload}))))

(defn novo-stub [bookmarks]
  (->StubMessageClient bookmarks (atom [])))
