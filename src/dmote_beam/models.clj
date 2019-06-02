;;; Geometry.

(ns dmote-beam.models
  (:require [scad-clj.model :as model]
            [scad-tarmi.core :refer [π]]
            [scad-tarmi.dfm :refer [error-fn]]
            [scad-tarmi.threaded :as threaded]))

(defn- basic-profile
  "A 2D profile, completely round or with rounded corners."
  [square diameter]
  (if square
    (model/square diameter diameter)
    (model/circle (/ diameter 2))))

(defn- beam-segment
  "A segment of the beam, optionally offset, optionally extruded."
  [{:keys [square beam-diameter diameter offset height]}]
  (let [o (if offset (partial model/offset offset) identity)
        h (if height (partial model/extrude-linear {:height height}) identity)]
    (->> (basic-profile square (or diameter beam-diameter)) o h)))

(defn- case-fastener-negative
  [{:keys [case-fastener-diameter holder-width]} compensator]
  (threaded/bolt :iso-size case-fastener-diameter
                 :head-type :countersunk
                 :unthreaded-length holder-width
                 :negative true
                 :compensator compensator))

(defn plate-anchor-model
  "Define geometry for a right-hand side case-to-beam anchor, for use with a
  DMOTE back plate, not a rear housing. This intended to hold not the beam
  itself but a threaded rod that runs through the beam (or is threaded into
  it), with a lock nut at one end of the anchor."
  [{:keys [spacing holder-thickness-factor holder-width
           beam-thickness beam-diameter case-fastener-diameter]
    :as opts}]
  (let [compensator (error-fn)
        bolt-head-diameter
          (compensator
            (threaded/datum case-fastener-diameter :countersunk-diameter))
        inner-diameter (- beam-diameter (* 2 beam-thickness))
        anchor-offset (* holder-thickness-factor inner-diameter)
        anchor-diameter (+ inner-diameter (* 2 anchor-offset))
        anchor-radius (/ anchor-diameter 2)
        half-radius (/ anchor-radius 2)
        plate-height (+ bolt-head-diameter 2)  ; Clear bevelling.
        plate-width (+ spacing plate-height)
        beam-displacement [0 anchor-radius (+ anchor-radius (/ plate-height 2))]]
    (model/difference
      (model/union
        ;; The beam holder:
        (model/translate beam-displacement
          (model/rotate [0 (/ π 2) 0]
            (beam-segment (merge opts {:diameter anchor-diameter
                                       :height plate-width}))))
        ;; A part connecting the beam holder to the plate:
        ;; This is a little bit taller and lower than the beam-holder quadrant
        ;; to cover the bevelling on the plate.
        (model/translate [0
                          (min half-radius (/ holder-width 2))
                          (+ half-radius (/ plate-height 2) -1)]
          (model/cube plate-width
                      (min anchor-radius holder-width)
                      (+ anchor-radius 2)))
        ;; The main body of the plate, with bevelled edges:
        (model/translate [0 (/ holder-width 2) 0]
          (model/hull
            ;; Full thickness.
            (model/cube (dec plate-width) holder-width (dec plate-height))
            (model/translate [0 -0.5 0]
              ;; Full height.
              (model/cube (dec plate-width) (dec holder-width) plate-height)
              ;; Full width.
              (model/cube plate-width (dec holder-width) (dec plate-height))))))
      ;; Space for a threaded rod:
      (model/translate beam-displacement
        (model/rotate [0 (/ π 2) 0]
          (beam-segment (merge opts {:diameter inner-diameter
                                     :height plate-width}))))
      ;; Spaces for screws:
      (apply model/union
        (for [divisor [-2 2]]
          (model/translate [(/ spacing divisor) holder-width 0]
            (model/rotate [(/ π -2) 0 0]
              (case-fastener-negative opts compensator))))))))

(defn funicular-model
  "Define geometry for a little widget that looks as if it hangs off the beam
  and holds up the wire for signalling between the two halves of the keyboard."
  [{:keys [holder-width holder-thickness-factor beam-diameter beam-thickness]
    :as opts}]
  (let [anchor-offset (* holder-thickness-factor beam-diameter)
        inner-diameter (- beam-diameter (* 2 beam-thickness))
        outer-diameter (+ beam-diameter 2)
        separation [outer-diameter 0 0]]
    (model/difference
      (model/hull
        (beam-segment (merge opts {:offset anchor-offset
                                   :height holder-width}))
        (model/translate separation
          (beam-segment (merge opts {:offset anchor-offset
                                     :height holder-width}))))
      (beam-segment (merge opts {:offset (- beam-thickness)
                                 :height holder-width}))
      (model/translate separation
        (beam-segment (merge opts {:height holder-width})))
      (model/translate [0 0 (/ holder-width 4)]
        (beam-segment (merge opts {:height (/ holder-width 2)}))))))

(defn clip-model
  "Define geometry for a clip that is threaded onto the beam.
  The clip is clamped tight by a perpendicular socket-cap fastener.
  This is intended for hollow beams that contain the signalling wire rather
  than a fastener. With a rear housing on the DMOTE, this
  substitutes for both ‘plate-anchor-model’ and ‘funicular-model’."
  [{:keys [holder-width holder-thickness-factor case-fastener-diameter
           beam-diameter beam-thickness clip-spacing]
    :or {clip-spacing 1}
    :as opts}]
  (let [offset (* holder-thickness-factor beam-diameter)
        clip-diameter (+ beam-diameter (* 2 offset))
        compensator (error-fn)
        compensated-beam-diameter (compensator beam-diameter)
        bolt-head-diameter
          (compensator (threaded/datum case-fastener-diameter :socket-diameter))
        sidecar (+ (/ compensated-beam-diameter 2)
                   (/ bolt-head-diameter 2)
                   clip-spacing)]
    (model/difference
      ;; Positive body:
      (model/union
        (model/rotate [0 (/ π 2) 0]
          (beam-segment (merge opts {:offset offset
                                     :height holder-width})))
        (model/translate [0 (/ sidecar 2) 0]
          (model/cube holder-width sidecar clip-diameter))
        (model/translate [0 sidecar 0]
          (model/cylinder (/ holder-width 2) clip-diameter)))
      ;; Negative space for the bolt that clamps the clip shut and connects it
      ;; to the keyboard case:
      (model/translate [0 sidecar 0]
        (model/union
          (model/cube holder-width holder-width 1)  ; Slit for clamping.
          (model/translate [0 0 (/ clip-diameter 2)]
            (threaded/bolt :iso-size case-fastener-diameter
                           :head-type :socket
                           :unthreaded-length clip-diameter
                           :negative true
                           :compensator compensator))))
      ;; Space for the beam:
      (model/rotate [0 (/ π 2) 0]
        (beam-segment (merge opts {:diameter compensated-beam-diameter
                                   :height (inc holder-width)}))))))
