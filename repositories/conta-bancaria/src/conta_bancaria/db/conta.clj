(ns conta-bancaria.db.conta
  (:require [com.stuartsierra.component :as component]))

(defrecord ContaDb [store]
  component/Lifecycle
  (start [this]
    (reset! store {})
    this)
  (stop [this] this))

(defn novo []
  (->ContaDb (atom {})))

(defn save! [{:keys [store]} conta]
  (swap! store assoc (:conta/id conta) conta)
  conta)

(defn find-by-id [{:keys [store]} id]
  (get @store id))
