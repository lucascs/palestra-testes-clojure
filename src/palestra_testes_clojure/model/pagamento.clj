(ns palestra-testes-clojure.model.pagamento
  "Schemas Plumatic para o payload de eventos de pagamento — usados pelo
   diplomat.autorizador como `:schema-resp` e `:schema` nos bookmarks."
  (:require [schema.core :as s]))

(s/defschema Autorizado
  {:pagamento-id s/Int
   :valor        BigDecimal})

(s/defschema Negado
  {:pagamento-id s/Int
   :valor        BigDecimal})
