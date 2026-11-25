(ns visualcraft.dxf-geometry
  "Pure-math 2D CAD and DXF geometry utilities: polyline length, shoelace
   polygon area, bounding boxes, DPI-to-mm calibration, architectural scale
   conversion, Ramer-Douglas-Peucker simplification, and ASCII DXF export."
  (:require [clojure.string :as str]))

(defn distance
  "Euclidean distance between 2D points [x1 y1] and [x2 y2]."
  [[x1 y1] [x2 y2]]
  (Math/hypot (- (double x2) (double x1))
              (- (double y2) (double y1))))

(defn polyline-length
  "Total length of an open or closed 2D polyline."
  ([pts] (polyline-length pts false))
  ([pts closed?]
   (if (< (count pts) 2)
     0.0
     (let [open-len (reduce + 0.0 (map distance pts (rest pts)))]
       (if (and closed? (> (count pts) 2))
         (+ open-len (distance (last pts) (first pts)))
         open-len)))))

(defn polygon-area
  "Unsigned area of a simple polygon using the shoelace formula."
  [pts]
  (let [n (count pts)]
    (if (< n 3)
      0.0
      (let [v (vec pts)
            acc (reduce (fn [s i]
                          (let [[x1 y1] (v i)
                                [x2 y2] (v (mod (inc i) n))]
                            (+ s (- (* (double x1) (double y2))
                                    (* (double x2) (double y1))))))
                        0.0
                        (range n))]
        (* 0.5 (Math/abs acc))))))

(defn bounding-box
  "Returns {:min-x :min-y :max-x :max-y :width :height} or nil when empty."
  [pts]
  (when (seq pts)
    (let [xs (map (comp double first) pts)
          ys (map (comp double second) pts)
          min-x (apply min xs)
          max-x (apply max xs)
          min-y (apply min ys)
          max-y (apply max ys)]
      {:min-x min-x
       :min-y min-y
       :max-x max-x
       :max-y max-y
       :width (- max-x min-x)
       :height (- max-y min-y)})))

(defn pixels->mm
  "Converts a raster pixel length at the given DPI to millimeters."
  [pixels dpi]
  (if (<= (double dpi) 0.0)
    0.0
    (* (/ (double pixels) (double dpi)) 25.4)))

(defn paper->model-scale
  "Converts a paper-space length using a 1:scale-denominator architectural scale."
  [paper-length scale-denominator]
  (if (<= (double scale-denominator) 0.0)
    0.0
    (* (double paper-length) (double scale-denominator))))

(defn- perpendicular-distance
  [[px py] [ax ay] [bx by]]
  (let [dx (- (double bx) (double ax))
        dy (- (double by) (double ay))]
    (if (and (zero? dx) (zero? dy))
      (distance [px py] [ax ay])
      (/ (Math/abs (+ (- (* dy (double px)) (* dx (double py)))
                      (- (* (double bx) (double ay))
                         (* (double by) (double ax)))))
         (Math/hypot dx dy)))))

(defn simplify-rdp
  "Simplifies a 2D polyline using the Ramer-Douglas-Peucker algorithm."
  [pts epsilon]
  (let [v (vec pts)
        n (count v)]
    (if (or (<= n 2) (<= (double epsilon) 0.0))
      v
      (let [first-pt (first v)
            last-pt (peek v)
            [max-idx max-dist]
            (reduce (fn [[bi bd] i]
                      (let [d (perpendicular-distance (v i) first-pt last-pt)]
                        (if (> d bd) [i d] [bi bd])))
                    [0 -1.0]
                    (range 1 (dec n)))]
        (if (> max-dist (double epsilon))
          (let [left (simplify-rdp (subvec v 0 (inc max-idx)) epsilon)
                right (simplify-rdp (subvec v max-idx) epsilon)]
            (into (pop left) right))
          [first-pt last-pt])))))

(defn format-dxf-polyline
  "Formats a 2D polyline as a minimal ASCII DXF LWPOLYLINE entity block."
  ([pts] (format-dxf-polyline pts "0" false))
  ([pts layer closed?]
   (let [layer-name (if (str/blank? layer) "0" layer)
         flag (if closed? 1 0)
         header ["0" "SECTION" "2" "ENTITIES"
                 "0" "LWPOLYLINE"
                 "8" layer-name
                 "90" (str (count pts))
                 "70" (str flag)]
         coords (mapcat (fn [[x y]]
                          ["10" (format "%.4f" (double x))
                           "20" (format "%.4f" (double y))])
                        pts)
         footer ["0" "ENDSEC" "0" "EOF"]]
     (str (str/join "\n" (concat header coords footer)) "\n"))))
