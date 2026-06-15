(ns palestra-testes-clojure.db.conta
  "Persistência de contas. Implementação em atom para os testes —
   em produção seria Datomic, DynamoDB, etc.")

(defn novo []
  (atom {:contas {}}))

(defn save! [db conta]
  (swap! db assoc-in [:contas (:conta/titular conta)] conta)
  conta)

(defn find-by-titular [db titular]
  (get-in @db [:contas titular]))

(defn all [db]
  (-> @db :contas vals vec))
