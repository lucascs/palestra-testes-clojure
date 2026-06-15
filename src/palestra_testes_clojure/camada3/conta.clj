(ns palestra-testes-clojure.conta
  "Modelo de conta em memória usada pelos exemplos de matcher-combinators
   e state-flow.")

(defn nova-conta [titular]
  {:titular     titular
   :saldo       0M
   :movimentos  []})

(defn deposita [conta valor]
  (-> conta
      (update :saldo + valor)
      (update :movimentos conj {:tipo :deposito :valor valor})))

(defn saca [conta valor]
  (when (< (:saldo conta) valor)
    (throw (ex-info "saldo insuficiente" {:saldo (:saldo conta) :valor valor})))
  (-> conta
      (update :saldo - valor)
      (update :movimentos conj {:tipo :saque :valor valor})))

(defn extrato [conta]
  {:titular    (:titular conta)
   :saldo      (:saldo conta)
   :movimentos (:movimentos conta)})
