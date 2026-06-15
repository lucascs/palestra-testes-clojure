(ns palestra-testes-clojure.camada5.db
  "Mini-banco em memória, atom-backed. A API imita o que faríamos
   contra um banco de verdade: save!, find-by-titular, all.")

(defn novo-db []
  (atom {:contas {}}))

(defn save! [db conta]
  (swap! db assoc-in [:contas (:conta/titular conta)] conta)
  conta)

(defn find-by-titular [db titular]
  (get-in @db [:contas titular]))

(defn all [db]
  (-> @db :contas vals vec))
