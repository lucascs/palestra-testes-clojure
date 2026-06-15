(ns palestra-testes-clojure.controller.conta
  "Orquestra a logic.conta sobre o db.conta — abre, deposita, saca,
   buscando e gravando a conta a cada operação."
  (:require [palestra-testes-clojure.db.conta    :as db]
            [palestra-testes-clojure.logic.conta :as logic]))

(defn abre! [db titular]
  (db/save! db (logic/nova titular)))

(defn deposita! [db titular valor]
  (let [conta (or (db/find-by-titular db titular)
                  (logic/nova titular))]
    (db/save! db (logic/deposita conta valor))))

(defn saca! [db titular valor]
  (if-let [conta (db/find-by-titular db titular)]
    (db/save! db (logic/saca conta valor))
    (throw (ex-info "conta inexistente"
                    {:conta/titular titular}))))
