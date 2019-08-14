(ns monstera.parser
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [inflections.core :as inflections]
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

(defn extract-species
  "Uses reaver library to parse page HTML data and query
  for the two list of plants, returning them as EDN maps."
  [page]
  (r/extract-from (r/parse page)
                  ".view-all-plants-list .view-content"
                  [:species]
                  ".field-content" r/edn))

(defn page->dossier
  "Given the destructured webpage body data, this function returns
  a list with species nodes classified by `toxic` and `non-toxic`."
  [animal page]
  (let [list (extract-species page)]
    {:animal animal
     :toxic (:species (first list))
     :non-toxic (:species (last list))}))

(defn url->animal
  "Identifies which animal a given url refers to."
  [url]
  (let [tokens (re-seq #"(/)\w+" url)]
    (-> (last tokens)
        (first)
        (str/replace "/" "")
        (inflections/plural))))

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

(defn ^:private parse-content
  [content]
  (-> (cond
        (string? content) (match-one-or-more content)
        (map? content)    (first (:content content))
        :default          content)))

(defn edn->taxon
  "Extracts a particular taxon info from species' descriptive EDN."
  [data key]
  (let [offset  (data-offset key)
        content (nth (:content data) offset)]
    (vector key (parse-content content))))

(defn ^:private map-default-taxons
  "Maps each taxon to its value and insert each pair taxon/value
  into a map."
  [data]
  (->> sp/taxons
       (map #(edn->taxon data %))
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

(defn ^:private parse-toxic-and-non-toxic-species
  [dossier]
  (let [animal (:animal dossier)]
    (into (map #(new-species % animal) (:toxic dossier))
          (map #(new-species %) (:non-toxic dossier)))))

(defn parse-species
  "Scraps species data from a remote webpage and parse into
  a coll of species maps."
  [url]
  (let [animal (url->animal url)]
    (->> (load-page url)
         (page->dossier animal)
         (parse-toxic-and-non-toxic-species))))
  
