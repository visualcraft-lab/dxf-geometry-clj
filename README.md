# dxf-geometry (Clojure)

Pure-math Clojure library for 2D CAD and DXF geometry calculations: polyline length, shoelace polygon area, bounding boxes, raster DPI-to-millimeter calibration, architectural drawing scale conversion, Ramer-Douglas-Peucker simplification, and ASCII DXF `LWPOLYLINE` formatting.

## Dependency

```clojure
;; deps.edn
{net.clojars.visualcraft/dxf-geometry {:mvn/version "0.1.0"}}
```

## Example

```clojure
(require '[visualcraft.dxf-geometry :as dxf])

(dxf/polyline-length [[0 0] [5000 0] [5000 4000] [0 4000]] true)
;; => 18000.0

(dxf/pixels->mm 300 300)
;; => 25.4

(dxf/simplify-rdp [[0 0] [5 0.05] [10 0] [10 10]] 0.2)
;; => [[0 0] [10 0] [10 10]]
```

## Online Reference

- Interactive image-to-CAD converter: [ImgToCAD](https://imgtocad.com/)
- Sketch vectorization reference: [Sketch to CAD](https://imgtocad.com/sketch-to-cad)

## License

MIT License — see [LICENSE](LICENSE).
