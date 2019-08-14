(defproject monstera "0.0.1-SNAPSHOT"
  :description "A survival guide for people in love with plants and pets"
  :dependencies [[clj-http "3.10.0"]
                 [inflections "0.13.2"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [org.clojure/math.combinatorics "0.1.5"]
                 [reaver "0.1.2"]]
  :profiles {:dev {:dependencies [[midje "1.9.8"]
                                  [nubank/matcher-combinators "1.0.1"]]
                   :plugins [[lein-midje "3.2.1"]
                             [lein-cloverage "1.0.9"]]}
             :midje {}})
