(ns palestra-testes-clojure.camada5.conta-test
  "Camada 5: state-flow onde o 'mundo' contém um banco em memória.
   O state é {:db (atom ...)} — controller.conta muta o atom; os asserts
   leem dele via state/gets."
  (:require [matcher-combinators.matchers :as m]
            [state-flow.api :refer [defflow flow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.state :as state]
            [palestra-testes-clojure.controller.conta :as conta]
            [palestra-testes-clojure.db.conta         :as db]))

;; ── Helpers que falam com o mundo ──────────────────────────────────────────

(defn- abre! [titular]
  (state/gets (fn [{:keys [db]}] (conta/abre! db titular))))

(defn- deposita! [titular valor]
  (state/gets (fn [{:keys [db]}] (conta/deposita! db titular valor))))

(defn- saca! [titular valor]
  (state/gets (fn [{:keys [db]}] (conta/saca! db titular valor))))

(defn- busca [titular]
  (state/gets (fn [{:keys [db]}] (db/find-by-titular db titular))))

(defn- todas []
  (state/gets (fn [{:keys [db]}] (db/all db))))

(defn- mundo-vazio []
  {:db (db/novo)})

;; ── Flows ──────────────────────────────────────────────────────────────────

(defflow uma-conta-movimenta
  {:init mundo-vazio}

  (flow "depósitos e saques se acumulam no banco"
    (abre!     "Lucas")
    (deposita! "Lucas" 100M)
    (deposita! "Lucas"  50M)
    (saca!     "Lucas"  30M)

    [extrato (busca "Lucas")]
    (match? {:conta/titular "Lucas"
             :conta/saldo   120M
             :conta/movimentos
             (m/embeds [{:movimento/tipo :saque :movimento/valor 30M}])}
            extrato)))

(defflow varias-contas-convivem
  {:init mundo-vazio}

  (flow "abertura de várias contas vai parar no db"
    (abre! "Lucas")
    (abre! "Ana")
    (deposita! "Lucas" 100M)
    (deposita! "Ana"    10M)

    [contas (todas)]
    (match? (m/in-any-order
              [{:conta/titular "Lucas" :conta/saldo 100M}
               {:conta/titular "Ana"   :conta/saldo  10M}])
            contas)))
