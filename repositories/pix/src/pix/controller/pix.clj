(ns pix.controller.pix
  (:require [pix.db.transacao :as db]))

(defn chave [{:keys [db]} chave-str]
  (db/registra-chave! db chave-str))

(defn transacao! [{:keys [db]} transacao]
  (db/registra-transacao! db transacao))

(defn transacoes [{:keys [db]}]
  (db/all-transacoes db))
