(ns monstera.entities.species
  (:require [clojure.spec.alpha :as spec])
  (:gen-class))

(spec/def ::species
          (spec/keys :req-un [::pop-name ::alt-names ::sci-name ::family]
                     :opt-un [::sources ::toxic-to]))

(def taxons [:pop-name :alt-names :sci-name :family])

(defn toxic-to?
  "Returns true if the animal is listed in the specie's
  :toxic-to list"
  [species animal]
  (-> (:toxic-to species)
      (.contains (name animal))))
