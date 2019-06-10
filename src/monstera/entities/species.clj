(ns monstera.entities.species
  (:gen-class))

(defrecord Species [sci-name pop-names toxic-to])

(defn toxic-to?
  "Returns true if the animal is listed in the specie's
  :toxic-to list"
  [species animal]
  (-> (:toxic-to species)
      (.contains (keyword animal))))