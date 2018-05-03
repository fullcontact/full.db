(ns full.db.sugar
  "SQL query sugar functions to be used with korma exec-raw"
  (:require [clojure.string :as s]))

(defn assemble-query
  "Assembles a query from multiple parts.
  `parts` is a sequence that can contain strings or (query, params) or
  nil which will be ignored"
  [parts]
  (let [[query-parts binding-parts]
        (->> parts
             (keep #(if (string? %) [% []] %))
             (apply map vector))]
    [(s/join " " query-parts) (apply concat binding-parts)]))

(defn- in-form
  [n]
  (str "(" (s/join ", " (repeat n "?")) ")"))

(defn in-part
  [query-part coll]
  {:pre [(seq coll)]}
  [(s/replace-first query-part "(?)" (in-form (count coll))) coll])
