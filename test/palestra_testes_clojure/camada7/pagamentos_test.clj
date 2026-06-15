(ns palestra-testes-clojure.camada7.pagamentos-test
  "Camada 7: testes de contrato, estilo Sachem.

   A ideia é a Lei de Postel: 'be conservative in what you do, be
   liberal in what you accept'. O produtor (autorizadora) publica um
   schema rico, com vários campos; o consumidor (nosso
   model.pagamento.Autorizado) só conhece um subconjunto. O contrato
   está OK se qualquer payload válido do produtor — depois de
   projetado para os campos que nos importam — continua válido contra
   o schema interno.

   O gerador test.check é construído sobre o schema externo; cada
   amostra simula uma mensagem que a autorizadora poderia publicar."
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as properties]
            [schema.core :as s]
            [palestra-testes-clojure.model.pagamento :as model.pagamento]))

;; ── Schema externo: tem todos os campos que precisamos, e vários a mais ────

(s/defschema AutorizadoExterno
  {:pagamento-id       s/Int
   :valor              BigDecimal
   :timestamp          java.util.Date
   :merchant-id        s/Str
   :authorization-code s/Str
   :card-last-digits   s/Str})

;; ── Generators construídos a partir das primitivas do schema externo ──────

(def gen-bigdecimal
  (gen/fmap #(BigDecimal/valueOf (long %)) gen/nat))

(def gen-date
  (gen/fmap #(java.util.Date. (long %)) (gen/large-integer* {:min 0})))

(def gen-autorizado-externo
  (gen/let [pagamento-id gen/nat
            valor        gen-bigdecimal
            ts           gen-date
            merchant     gen/string-alphanumeric
            auth-code    gen/string-alphanumeric
            last-digits  gen/string-alphanumeric]
    {:pagamento-id       pagamento-id
     :valor              valor
     :timestamp          ts
     :merchant-id        merchant
     :authorization-code auth-code
     :card-last-digits   last-digits}))

;; ── Sanity: o gerador respeita o próprio schema externo ────────────────────

(defspec gerador-bate-com-schema-externo
  50
  (properties/for-all [externo gen-autorizado-externo]
    (nil? (s/check AutorizadoExterno externo))))

;; ── Contrato: payload externo projetado é Autorizado interno ───────────────
;; Be liberal in what you accept: descartamos os campos que não conhecemos
;; e validamos só o que importa pra gente.

(defspec payload-externo-projetado-eh-autorizado-interno
  100
  (properties/for-all [externo gen-autorizado-externo]
    (let [interno (select-keys externo (keys model.pagamento/Autorizado))]
      (nil? (s/check model.pagamento/Autorizado interno)))))

;; ── E sem projetar, Plumatic rejeita por disallowed-key ────────────────────
;; — justifica o diplomat fazer a tradução de externo → interno antes de
;; entregar o payload pro resto da app. Sem essa projeção, qualquer campo
;; novo adicionado pelo produtor quebraria o consumidor.

(defspec payload-externo-sem-projetar-eh-rejeitado
  50
  (properties/for-all [externo gen-autorizado-externo]
    (some? (s/check model.pagamento/Autorizado externo))))
