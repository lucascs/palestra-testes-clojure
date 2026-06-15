(ns pix.model.transacao
  (:require [schema.core :as s]))

(s/defschema Chave s/Str)

(s/defschema Transacao
  {:pix/chave Chave
   :pix/valor BigDecimal})
