(ns conta-bancaria.controller.conta
  (:require [conta-bancaria.db.conta :as db]))

(defn cria [{:keys [db]} {:keys [titular saldo]}]
  (db/save! db {:conta/id      (random-uuid)
                :conta/titular titular
                :conta/saldo   saldo}))

(defn saldo [{:keys [db]} {:conta/keys [id]}]
  (:conta/saldo (db/find-by-id db id)))

(defn debita [{:keys [db]} {:conta/keys [id]} valor]
  (let [conta (db/find-by-id db id)]
    (db/save! db (update conta :conta/saldo - valor))))
