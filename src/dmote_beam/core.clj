;;; A CLI application for generating 3D models.

(ns dmote-beam.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :refer [make-parents]]
            [environ.core :refer [env]]
            [scad-app.core :refer [filter-by-name build-all]]
            [dmote-beam.models :as models])
  (:gen-class :main true))

(def cli-options
  "Define command-line interface flags."
  [["-V" "--version" "Print program version and exit"]
   ["-h" "--help" "Print this message and exit"]
   ["-r" "--render" "Render SCAD to STL"]
   [nil "--rendering-program PATH" "Path to OpenSCAD" :default "openscad"]
   ["-w" "--whitelist RE"
    "Limit output to files whose names match the regular expression RE"
    :default #"" :parse-fn re-pattern]
   [nil "--square"
    "Square-profile beam; the default is a cylinder"]
   ["-b" "--brim N"
    "Size of the plate around the center of each fastener"
    :default 8
    :parse-fn #(Integer/parseInt %)]
   [nil "--holder-thickness N"
    "Width of funicular and clip models"
    :default 6
    :parse-fn #(Float/parseFloat %)]
   [nil "--holder-width N"
    "Width of funicular and clip models"
    :default 15
    :parse-fn #(Integer/parseInt %)]
   ["-d" "--case-fastener-diameter N"
    "Size of each threaded fastener between beam holder and case"
    :default 6
    :parse-fn #(Integer/parseInt %)]
   ["-D" "--beam-fastener-diameter N"
    ;; So named because the beam can contain an actual threaded fastener.
    "Inner diameter of the beam itself"
    :default 6
    :parse-fn #(Float/parseFloat %)]
   ["-t" "--beam-thickness N"
    "Thickness of material in the beam, assuming it’s hollow"
    :default 1
    :parse-fn #(Float/parseFloat %)]
   [nil "--spacing N"
    "Distance between fasteners in the keyboard case back plate"
    :default 30
    :parse-fn #(Integer/parseInt %)]])

(defn write-files
  [{:keys [whitelist] :as options}]
  (let [roster [{:name "dmote-beam-clip",
                 :model-main (models/clip-model options)}
                {:name "dmote-beam-plate-anchor",
                 :model-main (models/plate-anchor-model options)}
                {:name "dmote-beam-funicular",
                 :model-main (models/funicular-model options)}]]
    (build-all (filter-by-name whitelist roster) options)))

(defn -main
  "Basic command-line interface logic."
  [& raw]
  (let [args (parse-opts raw cli-options)
        help-text (fn [] (println "dmote-beam options:")
                         (println (:summary args)))
        version (fn [] (println "dmote-beam version"
                         (env :dmote-beam-version)))
        error (fn [] (run! println (:errors args)) (System/exit 1))]
   (cond
     (get-in args [:options :help]) (help-text)
     (get-in args [:options :version]) (version)
     (some? (:errors args)) (error)
     :else (write-files (:options args)))))
