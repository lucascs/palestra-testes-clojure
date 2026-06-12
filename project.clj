(defproject palestra-testes-clojure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.stuartsierra/component "1.2.0"]
                 [nubank/matcher-combinators "3.10.0"]
                 [nubank/state-flow "5.20.1"]
                 [org.clojure/test.check "1.1.3"]
                 [prismatic/schema "1.4.1"]]
  :profiles {:dev {:source-paths ["dev"]}}
  :repl-options {:init-ns palestra-testes-clojure.core})
