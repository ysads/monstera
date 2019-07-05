(ns monstera.parser-test
  (:require [midje.sweet :refer :all]
            [monstera.parser :as parser]))

(fact "parser/urls equals to ASPCA links"
  parser/urls => ["https://www.aspca.org/pet-care/animal-poison-control/cats-plant-list"
                  "https://www.aspca.org/pet-care/animal-poison-control/dogs-plant-list"
                  "https://www.aspca.org/pet-care/animal-poison-control/horse-plant-list"])

