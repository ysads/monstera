(ns monstera.entities.species-test
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as spec]
            [midje.sweet :refer :all]
            [monstera.entities.species :as sp]))

(defn mock-new
  "Based on a given file path, reads an EDN fixture file and parse it
  into a valid clojure data structure"
  [edn-file-name]
  (-> (str "./test/fixtures/" edn-file-name ".edn")
      (slurp)
      (edn/read-string)))

(def sp-toxic-to-cat (mock-new "sp-toxic-to-cat"))
(def sp-toxic-to-dog (mock-new "sp-toxic-to-dog"))
(def sp-toxic-to-horse (mock-new "sp-toxic-to-horse"))

(facts "about - ::species spec"
  (fact "species with :sources and :toxic-to keys are valid"
    (spec/valid? :monstera.entities.species/species sp-toxic-to-cat) => true
    (spec/valid? :monstera.entities.species/species sp-toxic-to-dog) => true
    (spec/valid? :monstera.entities.species/species sp-toxic-to-horse) => true)

  (fact "species with only taxons are valid"
    (spec/valid? :monstera.entities.species/species
                 (dissoc sp-toxic-to-cat :toxic-to)) => true
    (spec/valid? :monstera.entities.species/species
                 (dissoc sp-toxic-to-dog :sources)) => true)

  (fact "species without taxons are not valid"
    (doseq [t sp/taxons]
      (spec/valid? :monstera.entities.species/species
                   (dissoc sp-toxic-to-dog t)) => false)))

(fact "sp/taxons is a list of which taxonomic information a species hold"
  sp/taxons => [:pop-name :alt-names :sci-name :family])

(facts "about - toxic-to?"
  (fact "it returns true if :toxic-to list contains the animal"
    (sp/toxic-to? sp-toxic-to-cat "cat") => true
    (sp/toxic-to? sp-toxic-to-dog "dog") => true
    (sp/toxic-to? sp-toxic-to-horse "horse") => true)

  (fact "it returns true if animal name is a keyword"
    (sp/toxic-to? sp-toxic-to-cat :cat) => true
    (sp/toxic-to? sp-toxic-to-dog :dog) => true
    (sp/toxic-to? sp-toxic-to-horse :horse) => true)

  (fact "it returns false if animal not in :toxic-to list"
    (sp/toxic-to? sp-toxic-to-cat "dog") => false
    (sp/toxic-to? sp-toxic-to-dog "cat") => false))
