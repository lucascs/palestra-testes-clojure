(ns pix.db.transacao
  (:require [com.stuartsierra.component :as component]))

(defrecord PixDb [store]
  component/Lifecycle
  (start [this]
    (reset! store {:chaves #{} :transacoes []})
    this)
  (stop [this] this))

(defn novo []
  (->PixDb (atom {:chaves #{} :transacoes []})))

(defn registra-chave! [{:keys [store]} chave]
  (swap! store update :chaves conj chave)
  chave)

(defn registra-transacao! [{:keys [store]} transacao]
  (swap! store update :transacoes conj transacao)
  transacao)

(defn all-transacoes [{:keys [store]}]
  (-> @store :transacoes))
