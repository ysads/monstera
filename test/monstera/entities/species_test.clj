(ns monstera.entities.species-test
  (:require [midje.sweet :refer :all]
            [monstera.entities.species :as sp]))

(def sp-toxic-to-cat {:toxic-to [:cats]})
(def sp-toxic-to-dog {:toxic-to [:dogs]})
(def sp-toxic-to-horse {:toxic-to [:horses]})

(facts "about `toxic-to?`"
  (fact "it returns true if :toxic-to list contains the arg"
    (sp/toxic-to? sp-toxic-to-cat :cats) => true
    (sp/toxic-to? sp-toxic-to-dog :dogs) => true
    (sp/toxic-to? sp-toxic-to-horse :horses) => true)

  (fact "it returns true if animal name is string"
    (sp/toxic-to? sp-toxic-to-cat "cats") => true)
    (sp/toxic-to? sp-toxic-to-dog "dogs") => true)

  (fact "it returns false if animal not in :toxic-to list"
    (sp/toxic-to? sp-toxic-to-cat :dogs) => false
    (sp/toxic-to? sp-toxic-to-dog :cats) => false))