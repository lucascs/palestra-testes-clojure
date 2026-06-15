(ns palestra-testes-clojure.camada8.dsl.pix
  "Helpers de state-flow que falam com o sistema do pix."
  (:require [state-flow.state :as state]
            [pix.controller.pix :as pix]))

(defn chave [chave-str]
  (state/gets (fn [{px :pix}] (pix/chave px chave-str))))

(defn transacoes []
  (state/gets (fn [{px :pix}] (pix/transacoes px))))
