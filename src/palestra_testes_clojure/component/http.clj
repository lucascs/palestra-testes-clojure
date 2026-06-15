(ns palestra-testes-clojure.component.http
  "Componente HTTP genérico. A produção (HttpClient) faria a chamada de
   fato; o stub (StubHttpClient) só registra as chamadas e devolve
   respostas pré-configuradas — é o que os state-flows usam.
   Os diplomats são quem traduz domínio em URLs concretas."
  (:require [com.stuartsierra.component :as component]))

(defprotocol HttpClient
  (GET  [this url])
  (POST [this url body]))

;; --- stub para testes ------------------------------------------------------

(defrecord StubHttpClient [respostas chamadas]
  component/Lifecycle
  (start [this]
    (reset! chamadas [])
    this)
  (stop  [this]
    this)

  HttpClient
  (GET [_ url]
    (swap! chamadas conj {:method :get :url url})
    (or (get @respostas [:get url])
        {:status 404}))
  (POST [_ url body]
    (swap! chamadas conj {:method :post :url url :body body})
    (or (get @respostas [:post url])
        {:status 201})))

(defn novo-stub
  "respostas: mapa {[:get \"/url\"] {:status 200 :body …}, …}"
  ([] (novo-stub {}))
  ([respostas]
   (->StubHttpClient (atom respostas) (atom []))))
