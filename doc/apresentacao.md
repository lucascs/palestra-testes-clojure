
# Testes em camada usando Clojure

**Lucas Cavalcanti** · @lucascs.bsky.social

---

# Camada 0 · REPL

Clojure: Lisp, REPL, estruturas imutáveis, funções como primitiva, interop com Java.

```clojure
;; Literais
"a"  \a  1  2.3M  22/7  :abc/def
#inst "2026-06-15T19:00:00"

;; Coleções imutáveis
[1 2 3]  '(1 2 3)  #{1 2 3}  {:a 1 :b 2}

;; Funções como valor
(def soma-um #(+ 1 %))
(map soma-um [1 2 3])    ;; => (2 3 4)

;; Threading
(->> [1 2 3 4]
     (filter even?)
     (reduce +))         ;; => 6
```

→ `dev/scratch.clj`

---

# Camada 1 · Testes de unidade

`clojure.test` (built-in): `deftest` + `testing` + `is`.

```clojure
(ns palestra-testes-clojure.camada1.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [palestra-testes-clojure.logic.math :as math]))

(deftest eh-par-test
  (testing "checa se o número é par"
    (is (math/eh-par? 2))
    (is (math/eh-par? 0))
    (is (not (math/eh-par? 3)))))
```

Roda com `lein test` ou `(clojure.test/run-tests)` no REPL.

---

# Camada 2 · Testes de propriedade

`test.check` — em vez de exemplo a exemplo, descrevemos uma **propriedade** que vale pra todo input gerado.

```clojure
(defspec multiplos-de-dois-sao-sempre-pares
  100
  (properties/for-all [n gen/small-integer]
    (math/eh-par? (* 2 n))))

(defspec soma-pares-ignora-impares
  100
  (properties/for-all [pares   (gen/vector (gen/fmap #(* 2 %) gen/small-integer))
                       impares (gen/vector (gen/fmap #(inc (* 2 %)) gen/small-integer))]
    (= (math/soma-pares pares)
       (math/soma-pares (concat pares impares)))))
```

Quando falha, encolhe o input até o **menor contraexemplo**.

---

# Camada 3 · Matcher-combinators

`nubank/matcher-combinators` — asserts sobre estruturas de dados, com match parcial e mensagens de erro diff-friendly.

```clojure
(let [extrato (-> (conta/nova "Lucas")
                  (conta/deposita 100M)
                  (conta/saca     30M)
                  conta/extrato)]
  (is (match? {:conta/titular "Lucas"
               :conta/saldo   70M}        ;; parcial: ignora movimentos
              extrato))
  (is (match? {:conta/movimentos
               (m/in-any-order
                 [{:movimento/tipo :saque    :movimento/valor 30M}
                  {:movimento/tipo :deposito :movimento/valor 100M}])}
              extrato)))
```

`m/equals`, `m/embeds`, `m/in-any-order`, `thrown-match?`…

---

# Camada 4 · Schemas

`plumatic/schema` para anotar shape do dado e contrato de função. Chaves namespaceadas, validação opt-in com `s/with-fn-validation`.

```clojure
(s/defschema Conta
  {:conta/titular    Titular
   :conta/saldo      BigDecimal
   :conta/movimentos [Movimento]})

(s/defn deposita :- Conta
  [conta :- Conta, valor :- Valor]
  (-> conta
      (update :conta/saldo + valor)
      (update :conta/movimentos conj
              {:movimento/tipo  :deposito
               :movimento/valor valor})))

(use-fixtures :each #(s/with-fn-validation (%)))
```

→ `model.conta`, `logic.conta`

---

# Camada 5 · State-flow com DB

`nubank/state-flow` — encadeia passos sobre um *world*. O state aqui é `{:db atom}`.

```clojure
(defflow uma-conta-movimenta
  {:init mundo-vazio}
  (flow "depósitos e saques se acumulam no banco"
    (abre!     "Lucas")
    (deposita! "Lucas" 100M)
    (saca!     "Lucas"  30M)

    [extrato (busca "Lucas")]
    (match? {:conta/saldo 70M
             :conta/movimentos
             (m/embeds [{:movimento/tipo :saque :movimento/valor 30M}])}
            extrato)))
```

→ `db.conta`, `controller.conta`

---

# Camada 6 · State-flow com componentes

State agora é um **system-map de Sierra Components** rodando: http-client, message-client, business component.

```clojure
(defflow autoriza-pagamento-aprovado
  {:init    (com-respostas {:autorizador/consulta-autorizacao {:status 200}})
   :cleanup component/stop}

  [resposta (autoriza! {:pagamento-id 123 :valor 100M})]
  (match? {:pagamento-id 123 :autorizado? true} resposta)

  [chamadas (chamadas-http)]
  (match? [{:bookmark :autorizador/consulta-autorizacao
            :url      "/autorizadora/123"}] chamadas)

  [msgs (mensagens-publicadas)]
  (match? [{:bookmark :pagamentos/autorizado
            :payload  {:pagamento-id 123 :valor 100M}}] msgs))
```

→ `component.{http,message}`, `diplomat.autorizador`, `controller.pagamentos`

---

# Camada 7 · Testes de contrato

**Postel**: *be conservative in what you do, be liberal in what you accept*. O produtor publica um schema rico; o consumidor depende de um subset.

```clojure
(s/defschema AutorizadoExterno          ;; produtor
  {:pagamento-id       s/Int
   :valor              BigDecimal
   :timestamp          java.util.Date
   :merchant-id        s/Str
   :authorization-code s/Str
   :card-last-digits   s/Str})

(defspec payload-externo-projetado-eh-autorizado-interno
  100
  (properties/for-all [externo gen-autorizado-externo]
    (let [interno (select-keys externo (keys model.pagamento/Autorizado))]
      (nil? (s/check model.pagamento/Autorizado interno)))))
```

Estilo Sachem: gera amostras do produtor, valida o subset consumidor.

---

# Camada 8 · Testes multi-serviço

Testar o comportamento em isolado de cada serviço não dá garantias o suficiente para fluxos mais complicados. 
Testar só o contrato é só uma validação sintática, não semântica.

```clojure
(def systems {:conta-bancaria (conta-bancaria.system/test-system)
              :pagamentos (pagamentos.system/test-system)
              :pix (pix.system/test-system)})

(defflow pagamento-do-pix-com-saldo-da-conta
   {:init    (inicia-multi-services systems)}
  
   [conta (conta-bancaria/cria-conta! {:titular "Lucas" :saldo 1000M})
    chave-pix (pix/chave "manda-o-pix")]
         (pagamentos/autoriza! {:origem conta :destino chave-pix :valor 300M})
         
   (match? 700M (conta-bancaria/saldo conta))
         
   (match? [{:chave "manda-o-pix"
             :valor 300M}] (pix/transacoes)))
```

---

# Recap

| Camada | Ferramenta | Cobertura |
|---|---|---|
| 0 | REPL | exploração |
| 1 | `clojure.test` | exemplo-a-exemplo |
| 2 | `test.check` | propriedade |
| 3 | `matcher-combinators` | dado |
| 4 | `plumatic/schema` | contrato de função |
| 5 | `state-flow` | fluxo + DB |
| 6 | `state-flow` + Sierra Components | fluxo + IO |
| 7 | `test.check` + schema | contrato externo |

**Cada camada cobre o que a anterior não cobre.**

---

# Obrigado!

**Lucas Cavalcanti** · @lucascs.bsky.social

Código: `github.com/...../palestra-testes-clojure`
