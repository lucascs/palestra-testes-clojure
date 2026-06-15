(ns palestra-testes-clojure.camada8.dsl.conta-bancaria
  "Helpers de state-flow que falam com o sistema de conta-bancaria."
  (:require [state-flow.state :as state]
            [conta-bancaria.controller.conta :as conta]))

(defn cria-conta! [cmd]
  (state/gets (fn [{cb :conta-bancaria}] (conta/cria cb cmd))))

(defn saldo [conta]
  (state/gets (fn [{cb :conta-bancaria}] (conta/saldo cb conta))))
