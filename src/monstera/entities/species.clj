(ns monstera.entities.species
  (:gen-class))

(def taxons [:pop-name :alt-names :sci-name :family])

(defn toxic-to?
  "Returns true if the animal is listed in the specie's
  :toxic-to list"
  [species animal]
  (-> (:toxic-to species)
      (.contains (keyword animal))))
