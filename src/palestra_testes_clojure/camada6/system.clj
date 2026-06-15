(ns palestra-testes-clojure.components.system
  "Montagem do system-map. Em produção você teria HttpClient real;
   aqui só expomos a versão de teste, parametrizada pelas respostas
   que o stub deve devolver."
  (:require [com.stuartsierra.component :as component]
            [palestra-testes-clojure.components.http-client    :as http]
            [palestra-testes-clojure.components.message-client :as msg]
            [palestra-testes-clojure.components.pagamentos     :as pagamentos]))

(defn test-system
  ([] (test-system {}))
  ([respostas-http]
   (component/system-map
     :http-client    (http/novo-stub respostas-http)
     :message-client (msg/novo-stub)
     :pagamentos     (component/using (pagamentos/novo)
                                      [:http-client :message-client]))))
