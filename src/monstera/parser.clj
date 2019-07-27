(ns monstera.parser
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [monstera.entities.species :as sp]
            [reaver :as r])
  (:gen-class))

(defn aspca-relative-url
  [endpoint]
  (str "https://www.aspca.org" endpoint))

;; This is not strictly needed, but, since there's already a function to
;; this, why not?
(def urls [(aspca-relative-url "/pet-care/animal-poison-control/cats-plant-list")
           (aspca-relative-url "/pet-care/animal-poison-control/dogs-plant-list")
           (aspca-relative-url "/pet-care/animal-poison-control/horse-plant-list")])

(defn load-page
  [url]
  (-> (:body (http/get url))
      (slurp)))

(defn extract-species-list
  "Uses reaver library to parse page HTML data and query
  for the two list of plants, returning them as EDN maps."
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

(def offsets (zipmap sp/taxons [0 1 4 7]))

(defn data-offset
  "Given a key, this functions returns the position in which this key
  is located within the array of contents."
  [key]
  (get offsets key))

(defn ^:private match-one-or-more
  "After matching the corresponding value, this function returns the
  plain value by itself, that is, not wrapped by any coll."
  [value]
  (let [match (->> (re-seq #"(\w+[\-\&\s]*)+" value)
                   (map #(str/trim (first %))))]
    (if (= 1 (count match))
      (first match)
      (vec match))))

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

(defn ^:private map-default-taxons
  "Maps each taxon to its value and insert each pair taxon/value
  into a map."
  [data]
  (->> sp/taxons
       (map (fn [key]
              (->> (edn->taxon data key)
                   (vector key))))
       (into {})))

(defn ^:private with-toxicity
  [species toxic-to]
  (if (nil? toxic-to)
    (assoc species :toxic-to [])
    (assoc species :toxic-to [toxic-to])))

(defn ^:private with-sources
  [species raw-species]
  (let [src-url (:href (:attrs (first (:content raw-species))))]
    (assoc species :sources [(aspca-relative-url src-url)])))

(defn new-species
  "Based on a single species data, parsed from page body, creates
  map containing its taxonomy, toxicity and source informations."
  ([data]
   (new-species data nil))
  ([data toxic-to]
   (-> data
       (map-default-taxons)
       (with-toxicity toxic-to)
       (with-sources data))))
