(ns pix.system
  (:require [com.stuartsierra.component :as component]
            [pix.db.transacao :as db]))

(defn test-system []
  (component/system-map
    :db (db/novo)))
