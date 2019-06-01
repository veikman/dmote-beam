;;; A CLI application for generating models for a connecting beam,
;;; an accessory to one possible configuration of the DMOTE keyboard.

(ns dmote-beam.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [scad-clj.scad :refer [write-scad]]
            [scad-clj.model :exclude [use import] :refer :all]
            [scad-tarmi.threaded :refer [datum]]))

(def plate-thickness 6)

(defn beam-shape
  "An item in the shape of the beam."
  [{is-square :square d0 :beam-fastener-diameter} d1 h]
  (extrude-linear {:height h}
    (if is-square
      (offset (/ (- d1 d0) 2)
        (square d0 d0))
      (circle (/ d1 2)))))

(defn case-fastener-negative
  [diameter]
  (let [radius (/ diameter 2)]
    (union
      (translate [0 0 (/ (+ plate-thickness 1) 2)]
        (cylinder diameter 1))
      (translate [0 0 (/ radius 2)]
        (cylinder [radius diameter] radius))
      (cylinder radius (inc plate-thickness)))))

(defn plate-anchor-model
  "Define geometry for the right-hand side."
  [opts]
  (let [{spacing :spacing brim :brim thickness :beam-thickness
         d0 :case-fastener-diameter d1 :beam-fastener-diameter} opts
        plate-height (* 2 brim)
        width (+ spacing plate-height)
        r1 (/ d1 2)
        rd (* 2 thickness)
        dd (+ d1 (* 2 rd))]
    (difference
      (union
        (translate [0 (+ rd (- r1 (/ (dec plate-thickness) 2))) (+ brim 4)]
          (rotate [0 (/ pi 2) 0]
            (beam-shape opts dd width)))
        (translate [0 0 brim]
          (cube width (dec plate-thickness) 8))
        (hull
          (translate [0 0.5 1]
            (cube (dec width) plate-thickness plate-height))
          (cube width (dec plate-thickness) plate-height)))
      (translate [0 (+ rd (- r1 (/ (dec plate-thickness) 2))) (+ brim 4)]
        (rotate [0 (/ pi 2) 0]
          (beam-shape opts d1 width)))
      (apply union
        (for [divisor [-2 2]]
          (translate [(/ spacing divisor) 0 0]
            (rotate [(/ pi -2) 0 0]
              (case-fastener-negative d0))))))))

(defn funicular-model
  "Define geometry for a little widget that hangs off the beam and holds
  up the wire for signalling between the two halves of the keyboard."
  [opts]
  (let [{width :clip-width thickness :beam-thickness
         d0 :beam-fastener-diameter} opts
        d1 (+ d0 (* 2 thickness))
        d2 (+ d1 2)
        separation [d2, 0, 0]]
   (difference
     (hull
       (beam-shape opts d2 width)
       (translate separation
         (beam-shape opts d2 width)))
     (beam-shape opts d0 width)
     (translate separation
       (beam-shape opts d0 width)
       (translate [0, 0, (/ width 4)]
         (beam-shape opts d1 (/ width 2)))))))

(defn clip-model
  "Define geometry for a clip that is threaded onto the beam.
  The clip is clamped tight by a perpendicular socket-cap fastener.
  This is intended for hollow beams that contain the signalling wire rather
  than a fastener. With a rear housing on the DMOTE, this
  substitutes for both ‘plate-anchor-model’ and ‘funicular-model’."
  [opts]
  (let [{width :clip-width
         cd0 :case-fastener-diameter
         bd0 :beam-fastener-diameter
         thickness :beam-thickness} opts
        cd1 (+ (datum cd0 :socket-diameter ) 0.4)
        cd2 (* 2 cd0)
        bd1 (+ (* 2 thickness) bd0)
        bd2 (* 1.25 bd1)
        bd3 (* 2 bd0)
        sidecar (+ (/ bd1 2) cd0)]
    (difference
      (union
        (rotate [0 (/ pi 2) 0]
          (beam-shape opts bd2 width))
        (translate [0 (- sidecar cd0) 0]
          (cube width cd2 bd2))
        (translate [0 sidecar 0]
          (cylinder (/ width 2) bd2)))
      (translate [0 sidecar 0]
        (union
          (cube width width 1)
          (translate [0 0 (- (/ bd2 2) (/ cd0 2))]
            (cylinder (/ cd1 2) cd0))
          (cylinder (/ cd0 2) (+ bd2 1))))
      (rotate [0 (/ pi 2) 0]
        (beam-shape opts bd0 (+ bd3 1))))))

(defn author
  "Author SCAD."
  [options]
  (spit "things/dmote-beam-clip.scad"
    (write-scad (clip-model options)))
  (spit "things/dmote-beam-plate-anchor.scad"
    (write-scad (plate-anchor-model options)))
  (spit "things/dmote-beam-funicular.scad"
    (write-scad (funicular-model options))))

(def cli-options
  "Define command-line interface."
  [[nil "--square"
    "Square-profile beam; the default is a cylinder"]
   ["-b" "--brim RADIUS"
    "Size of the plate around the center of each fastener"
    :default 8
    :parse-fn #(Integer/parseInt %)]
   ["-c" "--clip-width WIDTH"
    "Width of the funicular and clip models"
    :default 15
    :parse-fn #(Integer/parseInt %)]
   ["-d" "--case-fastener-diameter DIAMETER"
    "Size of each fastener between beam holder and case"
    :default 6
    :parse-fn #(Integer/parseInt %)]
   ["-D" "--beam-fastener-diameter DIAMETER"
    ;; So named because the beam can contain an actual threaded fastener.
    "Inner diameter of the beam itself"
    :default 6
    :parse-fn #(Float/parseFloat %)]
   ["-t" "--beam-thickness DIAMETER"
    "Thickness of material in the beam, assuming it’s hollow"
    :default 1
    :parse-fn #(Float/parseFloat %)]
   [nil "--spacing DISTANCE"
    "Distance between fasteners in the keyboard case back plate"
    :default 30
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--help" "Print this message"]])

(defn -main
  [& raw]
  (let [args (parse-opts raw cli-options)]
    (if (get-in args [:options :help])
      (println (:summary args))
      (if (nil? (:errors args))
        (author (:options args))
        (do (println (:summary args))
            (System/exit 1))))))
