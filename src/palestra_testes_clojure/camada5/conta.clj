(ns palestra-testes-clojure.camada5.conta
  "Camada 5: operações de conta que leem e escrevem em um db.
   Mantemos as chaves namespaceadas da Camada 4 e o mesmo shape de movimento."
  (:require [palestra-testes-clojure.camada5.db :as db]))

(defn- nova [titular]
  {:conta/titular    titular
   :conta/saldo      0M
   :conta/movimentos []})

(defn abre! [db titular]
  (db/save! db (nova titular)))

(defn deposita! [db titular valor]
  (let [conta  (or (db/find-by-titular db titular)
                   (nova titular))
        conta' (-> conta
                   (update :conta/saldo + valor)
                   (update :conta/movimentos conj
                           {:movimento/tipo  :deposito
                            :movimento/valor valor}))]
    (db/save! db conta')))

(defn saca! [db titular valor]
  (let [conta (db/find-by-titular db titular)]
    (when (or (nil? conta) (< (:conta/saldo conta) valor))
      (throw (ex-info "saldo insuficiente"
                      {:conta/titular   titular
                       :conta/saldo     (:conta/saldo conta 0M)
                       :movimento/valor valor})))
    (db/save! db
              (-> conta
                  (update :conta/saldo - valor)
                  (update :conta/movimentos conj
                          {:movimento/tipo  :saque
                           :movimento/valor valor})))))
