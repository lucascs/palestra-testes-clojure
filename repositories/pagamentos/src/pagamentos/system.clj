(ns pagamentos.system
  "Pagamentos não tem state próprio neste exemplo — coordena os outros.
   O system-map fica vazio mas existe pra manter a simetria com os
   outros serviços e permitir adicionar deps no futuro."
  (:require [com.stuartsierra.component :as component]))

(defn test-system []
  (component/system-map))
