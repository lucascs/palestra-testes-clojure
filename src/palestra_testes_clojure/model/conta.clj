(ns palestra-testes-clojure.model.conta
  "Schemas Plumatic descrevendo o shape dos dados de conta.
   Chaves usam keywords com namespace para deixar o domínio explícito."
  (:require [schema.core :as s]))

(s/defschema Titular
  (s/constrained s/Str seq "titular não pode ser vazio"))

(s/defschema Valor
  (s/constrained BigDecimal pos? "valor precisa ser positivo"))

(s/defschema Movimento
  {:movimento/tipo  (s/enum :deposito :saque)
   :movimento/valor Valor})

(s/defschema Conta
  {:conta/titular    Titular
   :conta/saldo      BigDecimal
   :conta/movimentos [Movimento]})
