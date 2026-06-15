(ns palestra-testes-clojure.camada8.dsl.pagamentos
  "Helpers de state-flow que falam com o sistema de pagamentos."
  (:require [state-flow.state :as state]
            [pagamentos.controller.pagamentos :as pagamentos]))

(defn autoriza! [cmd]
  (state/gets (fn [world] (pagamentos/autoriza! world cmd))))
