(ns pagamentos.controller.pagamentos
  "Coordena conta-bancaria e pix para autorizar um pagamento.
   Numa app real, esse controller falaria com os outros serviços via
   Kafka ou HTTP — aqui chamamos os controllers direto pra manter o
   exemplo de multi-serviço curto."
  (:require [conta-bancaria.controller.conta :as conta]
            [pix.controller.pix :as pix]))

(defn autoriza! [{conta-bancaria :conta-bancaria pix-system :pix}
                 {:keys [origem destino valor]}]
  (conta/debita conta-bancaria origem valor)
  (pix/transacao! pix-system {:pix/chave destino :pix/valor valor})
  {:autorizado? true})
