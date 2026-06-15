(ns palestra-testes-clojure.logic.conta
  "Operações puras sobre o agregado Conta. Não tocam DB nem IO —
   recebem uma conta e devolvem outra."
  (:require [schema.core :as s]
            [palestra-testes-clojure.model.conta :as model]))

(s/defn nova :- model/Conta
  [titular :- model/Titular]
  {:conta/titular    titular
   :conta/saldo      0M
   :conta/movimentos []})

(s/defn deposita :- model/Conta
  [conta :- model/Conta, valor :- model/Valor]
  (-> conta
      (update :conta/saldo + valor)
      (update :conta/movimentos conj
              {:movimento/tipo  :deposito
               :movimento/valor valor})))

(s/defn saca :- model/Conta
  [conta :- model/Conta, valor :- model/Valor]
  (when (< (:conta/saldo conta) valor)
    (throw (ex-info "saldo insuficiente"
                    {:conta/titular   (:conta/titular conta)
                     :conta/saldo     (:conta/saldo conta)
                     :movimento/valor valor})))
  (-> conta
      (update :conta/saldo - valor)
      (update :conta/movimentos conj
              {:movimento/tipo  :saque
               :movimento/valor valor})))

(s/defn extrato
  [conta :- model/Conta]
  (select-keys conta [:conta/titular :conta/saldo :conta/movimentos]))
