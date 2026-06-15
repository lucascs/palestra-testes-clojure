(ns palestra-testes-clojure.conta-schema
  "Mesma conta de palestra-testes-clojure.conta, agora com schemas
   Plumatic anotando contratos das funções e shape do dado.
   Chaves usam keywords com namespace para deixar o domínio explícito
   (`:conta/titular`, `:movimento/tipo`, …)."
  (:require [schema.core :as s]))

;; ── Schemas escalares ──────────────────────────────────────────────────────

(s/defschema Titular
  (s/constrained s/Str seq "titular não pode ser vazio"))

(s/defschema Valor
  (s/constrained BigDecimal pos? "valor precisa ser positivo"))

(s/defschema Saldo BigDecimal)

;; ── Schemas de domínio ─────────────────────────────────────────────────────

(s/defschema Movimento
  {:movimento/tipo  (s/enum :deposito :saque)
   :movimento/valor Valor})

(s/defschema Conta
  {:conta/titular    Titular
   :conta/saldo      Saldo
   :conta/movimentos [Movimento]})

(s/defschema Extrato
  {:conta/titular    Titular
   :conta/saldo      Saldo
   :conta/movimentos [Movimento]})

;; ── Operações ──────────────────────────────────────────────────────────────

(s/defn nova-conta :- Conta
  [titular :- Titular]
  {:conta/titular    titular
   :conta/saldo      0M
   :conta/movimentos []})

(s/defn deposita :- Conta
  [conta :- Conta, valor :- Valor]
  (-> conta
      (update :conta/saldo + valor)
      (update :conta/movimentos conj
              {:movimento/tipo  :deposito
               :movimento/valor valor})))

(s/defn saca :- Conta
  [conta :- Conta, valor :- Valor]
  (when (< (:conta/saldo conta) valor)
    (throw (ex-info "saldo insuficiente"
                    {:conta/saldo (:conta/saldo conta)
                     :movimento/valor valor})))
  (-> conta
      (update :conta/saldo - valor)
      (update :conta/movimentos conj
              {:movimento/tipo  :saque
               :movimento/valor valor})))

(s/defn extrato :- Extrato
  [conta :- Conta]
  (select-keys conta [:conta/titular :conta/saldo :conta/movimentos]))

;; ── Ligando / desligando as validações ─────────────────────────────────────
;;
;; Por padrão s/defn NÃO valida em runtime — gera só metadados. Para ativar
;; a validação (caro, use em dev/teste), embrulhe o código em:
;;
;;   (s/with-fn-validation
;;     (deposita (nova-conta "Lucas") -10M)) ; lança schema.core/ExceptionInfo
;;
;; Ou ligue globalmente com (s/set-fn-validation! true) no REPL.
