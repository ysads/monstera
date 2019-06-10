(ns monstera.entities.species-test
  (:require [midje.sweet :refer :all]
            [monstera.entities.species :refer :all]))

(def sp-toxic-to-cat {:toxic-to [:cats]})
(def sp-toxic-to-dog {:toxic-to [:dogs]})
(def sp-toxic-to-horse {:toxic-to [:horses]})

(facts "about `toxic-to?`"
  (fact "it returns true if :toxic-to list contains the arg"
    (toxic-to? sp-toxic-to-cat :cats) => true
    (toxic-to? sp-toxic-to-dog :dogs) => true
    (toxic-to? sp-toxic-to-horse :horses) => true)

  (fact "it returns true if animal name is string"
    (toxic-to? sp-toxic-to-cat "cats") => true)

  (fact "it returns false if animal not in :toxic-to list"
    (toxic-to? sp-toxic-to-cat :dogs) => false
    (toxic-to? sp-toxic-to-dog :cats) => false))