(ns conta-bancaria.system
  (:require [com.stuartsierra.component :as component]
            [conta-bancaria.db.conta :as db]))

(defn test-system []
  (component/system-map
    :db (db/novo)))
