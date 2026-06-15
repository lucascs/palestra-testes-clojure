(ns palestra-testes-clojure.system
  "Montagem do system-map. Em produção você teria HttpClient real;
   aqui só expomos a versão de teste. Os bookmarks dos diplomats
   são injetados nos componentes de IO."
  (:require [com.stuartsierra.component :as component]
            [palestra-testes-clojure.component.http        :as http]
            [palestra-testes-clojure.component.message     :as msg]
            [palestra-testes-clojure.controller.pagamentos :as pagamentos]
            [palestra-testes-clojure.diplomat.autorizador  :as autorizador]))

(defn test-system
  "respostas-http: mapa {<bookmark-key> {:status … :body …}}."
  ([] (test-system {}))
  ([respostas-http]
   (component/system-map
     :http-client    (http/novo-stub autorizador/http-bookmarks respostas-http)
     :message-client (msg/novo-stub  autorizador/topic-bookmarks)
     :pagamentos     (component/using (pagamentos/novo)
                                      [:http-client :message-client]))))
