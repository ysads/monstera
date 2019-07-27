(ns monstera.parser
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [monstera.entities.species :as sp]
            [reaver :as r])
  (:gen-class))

(def urls ["https://www.aspca.org/pet-care/animal-poison-control/cats-plant-list"
           "https://www.aspca.org/pet-care/animal-poison-control/dogs-plant-list"
           "https://www.aspca.org/pet-care/animal-poison-control/horse-plant-list"])

(def offsets (zipmap sp/taxons [0 1 4 7]))

(defn load-page
  "Downloads data from a given URL"
  [url]
  (-> (:body (http/get url))
      (slurp)))

(defn extract-species-list
  "Uses reaver library to parse page HTML data and query
  for the two list of plants, returning them as
  EDN maps."
  [page]
  (r/extract-from (r/parse page)
                  ".view-all-plants-list .view-content"
                  [:species]
                  ".field-content" r/edn))

(defn page->species-list
  "Given the destructured webpage body data, this function returns
  a list with species nodes classified by `toxic` and `non-toxic`."
  [animal page]
  (let [list (extract-species-list page)]
    {:animal animal
     :toxic (:species (first list))
     :non-toxic (:species (last list))}))

(defn data-offset
  "Given a key, this functions returns the position in which this key
  is located within the array of contents."
  [key]
  (get offsets key))

(defn match-one-or-more
  "After matching the corresponding value, this function returns the
  plain value by itself, that is, not wrapped by any coll."
  [value]
  (let [match (->> (re-seq #"(\w+[\-\&\s]*)+" value)
                   (map #(str/trim (first %))))]
    (if (= 1 (count match))
      (first match)
      match)))

(defn edn->taxon
  "Extracts a particular taxon info from species' descriptive EDN."
  [data key]
  (let [offset (data-offset key)
        val (nth (:content data) offset)]
    (cond
      (map? val)
      (first (:content val))

      (string? val)
      (match-one-or-more val)

      :default
      val)))

