(ns palestra-testes-clojure.logic.math
  "Funções puras de exemplo para Camadas 1 e 2.")

(defn eh-par?
  "Retorna true se `x` é um inteiro par."
  [x]
  (zero? (mod x 2)))

(defn eh-impar?
  "Retorna true se `x` é um inteiro ímpar."
  [x]
  (not (eh-par? x)))

(defn soma-pares
  "Soma todos os pares em `xs`."
  [xs]
  (->> xs (filter eh-par?) (reduce + 0)))
