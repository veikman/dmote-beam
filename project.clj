(defproject dmote-beam "0.1.0-SNAPSHOT"
  :description "Back beam model for the DMOTE keyboard"
  :url "http://viktor.eikman.se"
  :license {:name "GPL"
            :url "https://www.gnu.org/licenses/gpl.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.7"]
                 [unicode-math "0.2.0"]
                 [scad-clj "0.5.2"]]
  :main ^:skip-aot dmote-beam.core)
