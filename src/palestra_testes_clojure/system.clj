(ns palestra-testes-clojure.system
  "Montagem do system-map. Em produção você teria HttpClient real;
   aqui só expomos a versão de teste, parametrizada pelas respostas
   que o stub deve devolver."
  (:require [com.stuartsierra.component :as component]
            [palestra-testes-clojure.controller.pagamentos :as pagamentos]
            [palestra-testes-clojure.diplomat.http    :as http]
            [palestra-testes-clojure.diplomat.message :as msg]))

(defn test-system
  ([] (test-system {}))
  ([respostas-http]
   (component/system-map
     :http-client    (http/novo-stub respostas-http)
     :message-client (msg/novo-stub)
     :pagamentos     (component/using (pagamentos/novo)
                                      [:http-client :message-client]))))
