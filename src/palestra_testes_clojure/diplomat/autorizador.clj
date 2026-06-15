(ns palestra-testes-clojure.diplomat.autorizador
  "Diplomata da autorizadora externa. Concentra as chamadas HTTP e
   publicações de eventos relacionadas ao serviço de autorização —
   o controller fala em vocabulário de domínio, e este namespace é
   o único que conhece URLs e nomes de tópicos."
  (:require [palestra-testes-clojure.component.http    :as http]
            [palestra-testes-clojure.component.message :as msg]))

;; ── Bookmarks ──────────────────────────────────────────────────────────────
;; Mapas declarativos que reúnem os "endereços" que esse diplomat conhece.
;; Útil pra introspecção (logs, métricas, docs) e pra evitar strings soltas.

(def http-bookmarks
  {:consulta-autorizacao "/autorizadora/%s"})

(def topic-bookmarks
  {:autorizado :pagamentos/autorizado
   :negado     :pagamentos/negado})

(defn- url [bookmark & args]
  (apply format (get http-bookmarks bookmark) args))

;; ── Operações ──────────────────────────────────────────────────────────────

(defn consulta-autorizacao
  "Pergunta à autorizadora se um pagamento está aprovado.
   Devolve :autorizado ou :negado."
  [http-client pagamento-id]
  (let [{:keys [status]} (http/GET http-client (url :consulta-autorizacao pagamento-id))]
    (if (= 200 status) :autorizado :negado)))

(defn publica-resultado!
  "Publica o resultado da autorização no tópico correspondente."
  [message-client resultado pagamento]
  (msg/publish message-client
               (get topic-bookmarks resultado)
               pagamento))
