;;; A CLI application for generating models for a connecting beam,
;;; an accessory to one possible configuration of the DMOTE keyboard.

(ns dmote-beam.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [scad-clj.scad :refer [write-scad]]
            [scad-clj.model :exclude [use import] :refer :all]))

(def plate-thickness 6)

(defn case-fastener-negative
  [diameter]
  (let [radius (/ diameter 2)]
    (union
      (translate [0 0 (/ (+ plate-thickness 1) 2)]
        (cylinder diameter 1))
      (translate [0 0 (/ radius 2)]
        (cylinder [radius diameter] radius))
      (cylinder radius (inc plate-thickness)))))

(defn anchor-model
  "Define geometry for the right-hand side."
  [{spacing :spacing brim :brim
    d0 :case-fastener-diameter d1 :beam-fastener-diameter}]
  (let [plate-height (* 2 brim)
        width (+ spacing plate-height)
        r1 (/ d1 2)
        rd 2
        r2 (+ r1 rd)]
    (difference
      (union
        (translate [0 (+ rd (- r1 (/ (dec plate-thickness) 2))) (+ brim 4)]
          (rotate [0 (/ pi 2) 0]
            (cylinder r2 width)))
        (translate [0 0 brim]
          (cube width (dec plate-thickness) 8))
        (hull
          (translate [0 0.5 1]
            (cube (dec width) plate-thickness plate-height))
          (cube width (dec plate-thickness) plate-height)))
      (translate [0 (+ rd (- r1 (/ (dec plate-thickness) 2))) (+ brim 4)]
        (rotate [0 (/ pi 2) 0]
          (cylinder r1 width)))
      (apply union
        (for [divisor [-2 2]]
          (translate [(/ spacing divisor) 0 0]
            (rotate [(/ pi -2) 0 0]
              (case-fastener-negative d0))))))))

(defn funicular-model
  "Define geometry for a little widget that hangs off the beam and holds
  up the wire for signalling between the two halves of the keyboard."
  [{d0 :beam-fastener-diameter}]
  (let [r (fn [d] (/ d 2))
        d1 (+ d0 2)
        d2 (+ d1 2)
        h 10
        separation [d2, 0, 0]]
   (difference
     (hull
       (cylinder (r d2) h)
       (translate separation
         (cylinder (r d2) h)))
     (cylinder (r d0) h)
     (translate separation
       (cylinder (r d0) h)
       (translate [0, 0, (/ h 4)]
         (cylinder (r d1) (/ h 2)))))))

(defn author
  "Author SCAD."
  [options]
  (spit "things/dmote-beam-anchor.scad" (write-scad (anchor-model options)))
  (spit "things/dmote-beam-funicular.scad" (write-scad (funicular-model options))))

(def cli-options
  "Define command-line interface."
  [["-s" "--spacing DISTANCE"
    "Distance between the two fasteners connecting to the keyboard case"
    :default 30
    :parse-fn #(Integer/parseInt %)]
   ["-b" "--brim RADIUS"
    "Size of the plate around the center of each fastener"
    :default 8
    :parse-fn #(Integer/parseInt %)]
   ["-d" "--case-fastener-diameter DIAMETER"
    "Size of each fastener between beam holder and case"
    :default 6
    :parse-fn #(Integer/parseInt %)]
   ["-D" "--beam-fastener-diameter DIAMETER"
    "Size of the beam itself"
    :default 6
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--help"]])

(defn -main
  [& raw]
  (let [args (parse-opts raw cli-options)]
    (if (nil? (:errors args))
      (author (:options args))
      (do (println (:summary args))
          (System/exit 1)))))
