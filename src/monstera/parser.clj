(ns monstera.parser
  (:require [reaver :as r])
  (:gen-class))

(def urls ["https://www.aspca.org/pet-care/animal-poison-control/cats-plant-list"
           "https://www.aspca.org/pet-care/animal-poison-control/dogs-plant-list"
           "https://www.aspca.org/pet-care/animal-poison-control/horse-plant-list"])

; (extract-from (parse content) ".field-content"
;               [:main-name :sci-name :family]
;               "a" text
;               "i" text)