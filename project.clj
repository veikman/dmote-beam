(defproject dmote-beam "0.1.0-SNAPSHOT"
  :description "Back beam model for the DMOTE keyboard"
  :url "http://viktor.eikman.se"
  :license {:name "GPL"
            :url "https://www.gnu.org/licenses/gpl.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.3.7"]
                 [scad-clj "0.5.2"]
                 [scad-tarmi "0.4.0"]]
  :main ^:skip-aot dmote-beam.core)
