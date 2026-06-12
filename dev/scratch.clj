(ns scratch
  "Estruturas básicas de Clojure — para mandar forma a forma no REPL.
   Não é namespace de produção: vive em ./dev/ para não poluir src/."
  (:require [clojure.string :as str]))

;; ── Literais ────────────────────────────────────────────────────────────────
"a"                            ; string
\a                             ; caractere
1                              ; long
1.5                            ; double
2.3M                           ; bigdecimal (M = Money)
22/7                           ; ratio (exato!)
true                           ; boolean
nil                            ; nil

:abc                           ; keyword
:abc/def                       ; keyword com namespace
::local                        ; keyword auto-namespaced (= :scratch/local)

'simbolo                       ; symbol
#inst "2026-06-15T19:00:00"    ; java.util.Date
#uuid "00000000-0000-0000-0000-000000000000"
#"\d+"                         ; regex (java.util.regex.Pattern)

;; ── Coleções imutáveis ─────────────────────────────────────────────────────
[1 2 3]                        ; vector — acesso por índice O(1) amortizado
'(1 2 3)                       ; lista — linked list, push em O(1)
#{1 2 3}                       ; set — sem duplicatas
{:a 1 :b 2}                    ; map
{:saldo 100M :titular "Lucas"} ; valores podem ser de qualquer tipo

;; "imutável" = toda operação devolve uma nova coleção
(let [v [1 2 3]]
  [v (conj v 4) (assoc v 0 :zero)])
;; => [[1 2 3] [1 2 3 4] [:zero 2 3]]

;; ── Funções como valor ─────────────────────────────────────────────────────
(def soma-um (fn [a] (+ 1 a)))
(soma-um 41)                    ; => 42

;; açúcar sintático para fn:
(def soma-um* #(+ 1 %))

;; defn = def + fn + docstring
(defn dobra
  "Multiplica por 2."
  [x]
  (* 2 x))

(map dobra [1 2 3])             ; => (2 4 6)

;; funções de ordem superior
(->> [1 2 3 4 5]
     (filter odd?)
     (map dobra)
     (reduce +))                ; => 18

;; ── Destructuring ──────────────────────────────────────────────────────────
(let [[x y & resto]      [1 2 3 4 5]
      {:keys [a b]}      {:a 10 :b 20}
      {titular :titular} {:titular "Lucas"}]
  {:x x :y y :resto resto :a a :b b :titular titular})

;; ── Threading macros ───────────────────────────────────────────────────────
(-> "  Lucas Cavalcanti  "
    str/trim
    str/lower-case
    (str/replace " " "-"))      ; => "lucas-cavalcanti"

;; ── Interop com Java ───────────────────────────────────────────────────────
(.toUpperCase "abc")            ; chamada de método
(java.util.UUID/randomUUID)     ; método estático
(.getYear (java.time.LocalDate/now))

;; ── REPL como ferramenta de desenvolvimento ────────────────────────────────
;; mande qualquer forma acima para o REPL e veja o valor.
;; (doc dobra)     ;; mostra a docstring
;; (source dobra)  ;; mostra o source
