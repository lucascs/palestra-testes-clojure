(ns conta-bancaria.model.conta
  (:require [schema.core :as s]))

(s/defschema Conta
  {:conta/id      java.util.UUID
   :conta/titular s/Str
   :conta/saldo   BigDecimal})
