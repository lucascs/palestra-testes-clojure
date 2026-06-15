(ns palestra-testes-clojure.component.http
  "Componente HTTP. Recebe um mapa de bookmarks na construção: cada
   bookmark conhece sua URL template (com `:placeholders`) e pode anotar
   schemas, service, etc. As chamadas referenciam apenas a chave do
   bookmark e os `:path-params` — o componente faz a interpolação."
  (:require [clojure.string :as str]
            [com.stuartsierra.component :as component]))

(defprotocol HttpClient
  (GET  [this opts])
  (POST [this opts]))

(defn- interpolate [template path-params]
  (reduce-kv (fn [u k v]
               (str/replace u (str ":" (name k)) (str v)))
             template
             (or path-params {})))

(defn resolve-url [bookmarks bookmark path-params]
  (-> bookmarks (get bookmark) :url (interpolate path-params)))

;; --- stub para testes ------------------------------------------------------

(defrecord StubHttpClient [bookmarks respostas chamadas]
  component/Lifecycle
  (start [this]
    (reset! chamadas [])
    this)
  (stop  [this]
    this)

  HttpClient
  (GET [_ {:keys [bookmark path-params]}]
    (let [url (resolve-url bookmarks bookmark path-params)]
      (swap! chamadas conj {:method :get :bookmark bookmark :url url})
      (or (get @respostas bookmark)
          {:status 404})))
  (POST [_ {:keys [bookmark path-params body]}]
    (let [url (resolve-url bookmarks bookmark path-params)]
      (swap! chamadas conj {:method :post :bookmark bookmark :url url :body body})
      (or (get @respostas bookmark)
          {:status 201}))))

(defn novo-stub
  "respostas: mapa {:bookmark-key {:status 200 :body …}, …}.
   Bookmarks vêm do diplomat dono."
  ([bookmarks] (novo-stub bookmarks {}))
  ([bookmarks respostas]
   (->StubHttpClient bookmarks (atom respostas) (atom []))))
