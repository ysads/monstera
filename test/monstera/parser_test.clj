(ns monstera.parser-test
  (:require [clojure.spec.alpha :as spec]
            [midje.sweet :refer :all]
            [monstera.entities.species :as sp]
            [monstera.parser :as parser]
            [matcher-combinators.midje :refer [match]]
            [matcher-combinators.matchers :as m]))

(def page (slurp "./test/fixtures/cats-list.txt"))

(fact "parser/aspca-relative-url appends endpoint to base url"
  (parser/aspca-relative-url "") => "https://www.aspca.org"
  (parser/aspca-relative-url "/abc/1") => "https://www.aspca.org/abc/1"
  (parser/aspca-relative-url "/pet-control?x=1") => "https://www.aspca.org/pet-control?x=1")

(fact "parser/urls are ASPCA urls"
  parser/urls => ["https://www.aspca.org/pet-care/animal-poison-control/cats-plant-list"
                  "https://www.aspca.org/pet-care/animal-poison-control/dogs-plant-list"
                  "https://www.aspca.org/pet-care/animal-poison-control/horse-plant-list"])

(fact "parser/page->dossier returns two list of species nodes"
  (parser/page->dossier "cat" page) => (just {:animal "cat"
                                                   :toxic list?
                                                   :non-toxic list?}))

(fact "parser/url->animal extracts animal name from ASPCA url"
  (let [urls parser/urls]
    (parser/url->animal (nth urls 0)) => "cats"
    (parser/url->animal (nth urls 1)) => "dogs"
    (parser/url->animal (nth urls 2)) => "horses"))

(fact "parser/data-offset maps a given taxonomy key to where it's located within
  contents array"
  (parser/data-offset :pop-name) => 0
  (parser/data-offset :alt-names) => 1
  (parser/data-offset :sci-name) => 4
  (parser/data-offset :family) => 7)

(def species-list (parser/page->dossier "cat" page))
(def species-one (first (:toxic species-list)))
(def species-two (last (:toxic species-list)))
(def alt-names ["Arum" "Lord-and-Ladies" "Wake Robin" "Starch Root" "Bobbins" "Cuckoo Plant"])

(facts "about - parser/end->taxon"
  (fact "it returns own data if it's plain string"
    (parser/edn->taxon species-one :family) => [:family "Araceae"]
    (parser/edn->taxon species-two :family) => [:family "Agavaceae"])
  
  (fact "if data is a comma-separated string, split it into array"
    (parser/edn->taxon species-two :alt-names) => [:alt-names []]
    (parser/edn->taxon species-one :alt-names) => (match [:alt-names (just alt-names :in-any-order)]))

  (fact "it returns :content attribute if it's an EDN-described element"
    (parser/edn->taxon species-one :pop-name) => [:pop-name "Adam-and-Eve"]
    (parser/edn->taxon species-two :pop-name) => [:pop-name "Yucca"]
    (parser/edn->taxon species-one :sci-name) => [:sci-name "Arum maculatum"]
    (parser/edn->taxon species-two :sci-name) => [:sci-name "Yucca spp."]))

(fact "parser/new-species creates a map for a given species"
  (parser/new-species species-one "cats") => (match {:pop-name  "Adam-and-Eve"
                                                     :alt-names (just alt-names :in-any-order)
                                                     :sci-name  "Arum maculatum"
                                                     :family    "Araceae"
                                                     :toxic-to  ["cats"]
                                                     :sources   ["https://www.aspca.org/pet-care/animal-poison-control/toxic-and-non-toxic-plants/adam-and-eve"]})
  (parser/new-species species-two) => (match {:pop-name  "Yucca"
                                              :alt-names []
                                              :sci-name  "Yucca spp."
                                              :family    "Agavaceae"
                                              :toxic-to  []
                                              :sources   ["https://www.aspca.org/pet-care/animal-poison-control/toxic-and-non-toxic-plants/yucca"]}))

(defn valid-species?
  [spp]
  (spec/valid? :monstera.entities.species/species spp))

(def toxic-to-cat?
  (fn [spp]
    (.contains (:toxic-to spp) "cat")))

(fact "parser/parse-species returns a coll of maps, each a single species"
  (against-background [(parser/load-page ..url..) => page
                       (parser/url->animal ..url..) => "cat"])
  (let [species (parser/parse-species ..url..)]
    species => (has every? valid-species?)
    (count (filter toxic-to-cat? species)) => 417
    (count (filter (complement toxic-to-cat?) species)) => 569))
