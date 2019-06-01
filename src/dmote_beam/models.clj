;;; Geometry.

(ns dmote-beam.models
  (:require [scad-clj.model :as model]
            [scad-tarmi.core :refer [π]]
            [scad-tarmi.threaded :refer [datum]]))

(defn- beam-shape
  "An item in the shape of the beam."
  [{:keys [square beam-fastener-diameter]} d1 h]
  (model/extrude-linear {:height h}
    (if square
      (model/offset (/ (- d1 beam-fastener-diameter) 2)
        (model/square beam-fastener-diameter beam-fastener-diameter))
      (model/circle (/ d1 2)))))

(defn- case-fastener-negative
  [{:keys [case-fastener-diameter holder-thickness]}]
  (let [radius (/ case-fastener-diameter 2)]
    (model/union
      (model/translate [0 0 (/ (+ holder-thickness 1) 2)]
        (model/cylinder case-fastener-diameter 1))
      (model/translate [0 0 (/ radius 2)]
        (model/cylinder [radius case-fastener-diameter] radius))
      (model/cylinder radius (inc holder-thickness)))))

(defn plate-anchor-model
  "Define geometry for the right-hand side."
  [{:keys [spacing brim holder-thickness beam-thickness case-fastener-diameter
           beam-fastener-diameter]
    :as opts}]
  (let [plate-height (* 2 brim)
        width (+ spacing plate-height)
        r1 (/ beam-fastener-diameter 2)
        rd (* 2 beam-thickness)
        dd (+ beam-fastener-diameter (* 2 rd))]
    (model/difference
      (model/union
        (model/translate [0 (+ rd (- r1 (/ (dec holder-thickness) 2))) (+ brim 4)]
          (model/rotate [0 (/ π 2) 0]
            (beam-shape opts dd width)))
        (model/translate [0 0 brim]
          (model/cube width (dec holder-thickness) 8))
        (model/hull
          (model/translate [0 0.5 1]
            (model/cube (dec width) holder-thickness plate-height))
          (model/cube width (dec holder-thickness) plate-height)))
      (model/translate [0 (+ rd (- r1 (/ (dec holder-thickness) 2))) (+ brim 4)]
        (model/rotate [0 (/ π 2) 0]
          (beam-shape opts beam-fastener-diameter width)))
      (apply model/union
        (for [divisor [-2 2]]
          (model/translate [(/ spacing divisor) 0 0]
            (model/rotate [(/ π -2) 0 0]
              (case-fastener-negative opts))))))))

(defn funicular-model
  "Define geometry for a little widget that hangs off the beam and holds
  up the wire for signalling between the two halves of the keyboard."
  [{:keys [holder-width beam-thickness beam-fastener-diameter]
    :as opts}]
  (let [d1 (+ beam-fastener-diameter (* 2 beam-thickness))
        d2 (+ d1 2)
        separation [d2, 0, 0]]
   (model/difference
     (model/hull
       (beam-shape opts d2 holder-width)
       (model/translate separation
         (beam-shape opts d2 holder-width)))
     (beam-shape opts beam-fastener-diameter holder-width)
     (model/translate separation
       (beam-shape opts beam-fastener-diameter holder-width)
       (model/translate [0, 0, (/ holder-width 4)]
         (beam-shape opts d1 (/ holder-width 2)))))))

(defn clip-model
  "Define geometry for a clip that is threaded onto the beam.
  The clip is clamped tight by a perpendicular socket-cap fastener.
  This is intended for hollow beams that contain the signalling wire rather
  than a fastener. With a rear housing on the DMOTE, this
  substitutes for both ‘plate-anchor-model’ and ‘funicular-model’."
  [{:keys [holder-width case-fastener-diameter beam-fastener-diameter
           beam-thickness]
    :as opts}]
  (let [cd1 (+ (datum case-fastener-diameter :socket-diameter) 0.4)
        cd2 (* 2 case-fastener-diameter)
        bd1 (+ (* 2 beam-thickness) beam-fastener-diameter)
        bd2 (* 1.25 bd1)
        bd3 (* 2 beam-fastener-diameter)
        sidecar (+ (/ bd1 2) case-fastener-diameter)]
    (model/difference
      (model/union
        (model/rotate [0 (/ π 2) 0]
          (beam-shape opts bd2 holder-width))
        (model/translate [0 (- sidecar case-fastener-diameter) 0]
          (model/cube holder-width cd2 bd2))
        (model/translate [0 sidecar 0]
          (model/cylinder (/ holder-width 2) bd2)))
      (model/translate [0 sidecar 0]
        (model/union
          (model/cube holder-width holder-width 1)
          (model/translate [0 0 (- (/ bd2 2) (/ case-fastener-diameter 2))]
            (model/cylinder (/ cd1 2) case-fastener-diameter))
          (model/cylinder (/ case-fastener-diameter 2) (+ bd2 1))))
      (model/rotate [0 (/ π 2) 0]
        (beam-shape opts beam-fastener-diameter (+ bd3 1))))))
