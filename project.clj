(defproject monstera "0.0.1-SNAPSHOT"
  :description "A survival guide for people in love with plants and pets"
  :dependencies [[aleph "0.4.6"]
                 [org.clojure/clojure "1.7.0"]
                 [reaver "0.1.2"]]
  :profiles {:dev {:dependencies [[midje "1.7.0"]]
                   :plugins [[lein-midje "3.2.1"]]}
             :midje {}})
