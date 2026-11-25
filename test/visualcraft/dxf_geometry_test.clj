(ns visualcraft.dxf-geometry-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [visualcraft.dxf-geometry :as dxf]))

(deftest polyline-and-area-test
  (let [sq [[0 0] [10 0] [10 10] [0 10]]]
    (is (< (Math/abs (- 30.0 (dxf/polyline-length sq false))) 1e-9))
    (is (< (Math/abs (- 40.0 (dxf/polyline-length sq true))) 1e-9))
    (is (< (Math/abs (- 100.0 (dxf/polygon-area sq))) 1e-9))))

(deftest scale-and-simplify-test
  (is (< (Math/abs (- 25.4 (dxf/pixels->mm 300 300))) 1e-9))
  (is (< (Math/abs (- 1000.0 (dxf/paper->model-scale 20 50))) 1e-9))
  (let [pts [[0 0] [5 0.05] [10 0] [10 10]]
        simplified (dxf/simplify-rdp pts 0.2)]
    (is (= 3 (count simplified)))
    (is (str/includes? (dxf/format-dxf-polyline simplified "WALLS" true) "LWPOLYLINE"))))
