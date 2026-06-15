(ns palestra-testes-clojure.diplomat.autorizador
  "Diplomata da autorizadora externa. Concentra as chamadas HTTP e
   publicações de eventos relacionadas ao serviço de autorização —
   o controller fala em vocabulário de domínio, e este namespace é
   o único que conhece URLs e nomes de tópicos."
  (:require [palestra-testes-clojure.component.http    :as http]
            [palestra-testes-clojure.component.message :as msg]
            [palestra-testes-clojure.model.pagamento   :as model.pagamento]))

;; ── Bookmarks ──────────────────────────────────────────────────────────────
;; Mapas declarativos que reúnem os "endereços" que esse diplomat conhece.
;; Útil pra introspecção (logs, métricas, docs) e pra evitar strings soltas.
;; Os componentes recebem esses mapas na construção e resolvem URL/tópico
;; sozinhos — aqui a gente só passa a chave do bookmark.

(def http-bookmarks
  {:autorizador/consulta-autorizacao {:url         "/autorizadora/:pagamento-id"
                                      :service     :authorizador
                                      :schema-resp {200 model.pagamento/Autorizado
                                                    400 model.pagamento/Negado}}})

(def topic-bookmarks
  {:pagamentos/autorizado {:topic  "PAGAMENTOS.AUTORIZADO"
                           :schema model.pagamento/Autorizado}
   :pagamentos/negado     {:topic  "PAGAMENTOS.NEGADO"
                           :schema model.pagamento/Negado}})

;; ── Operações ──────────────────────────────────────────────────────────────

(defn consulta-autorizacao
  "Pergunta à autorizadora se um pagamento está aprovado.
   Devolve `:pagamentos/autorizado` ou `:pagamentos/negado` —
   já é a chave do topic-bookmark, pronta pra `publica-resultado!`."
  [http-client pagamento-id]
  (let [{:keys [status]} (http/GET http-client
                                   {:bookmark    :autorizador/consulta-autorizacao
                                    :path-params {:pagamento-id pagamento-id}})]
    (if (= 200 status) :pagamentos/autorizado :pagamentos/negado)))

(defn publica-resultado!
  "Publica o resultado da autorização no tópico correspondente."
  [message-client resultado pagamento]
  (msg/publish message-client resultado pagamento))
