(ns ledger-observer.visualization.materials
  (:require three
            [ledger-observer.visualization.render-utils :as render-utils]))

#_(def sprite (.load (three/TextureLoader.) "images/circle.svg"))




(def yellow [240 162 2])
(def blue [212 20 90])
(def red [10 147 165])
(def error-red [100  0 20])
(def dark-red [118 12 76])
(def dark-blue [  0 48 73])

(def base-v 60)
(def base [base-v base-v base-v])
(def brighter-base (map #(+ 80 %) base))

(def marked-line-material
  (three/LineBasicMaterial.
    (clj->js
      {:blending    three/AdditiveBlending
       :linewidth   2
       :color       (let [[r g b] (map #(/ % 255) yellow)]
                     (three/Color. r g b))
       :transparent false})))



(defn between-colors [[r g b] [base-r base-g base-b]]
  (let [r-step (/ (- r base-r) 255)
        g-step (/ (- g base-g) 255)
        b-step (/ (- b base-b) 255)
        gr (/ base-r 255)
        gg (/ base-g 255)
        gb (/ base-b 255)]
    (fn [ratio]
      [(+ gr (* ratio r-step))
       (+ gg (* ratio g-step))
       (+ gb (* ratio b-step))])))


(def white [255 255 255])

(def base->brighter-base (between-colors brighter-base base))

(let [[base-r base-g base-b] base
      gr                           (/ base-r 255)
      gg                           (/ base-g 255)
      gb                           (/ base-b 255)]

  (defn base-line-material* [temp]
    (let [ratio (/ temp render-utils/max-count)
          [r g b] (base->brighter-base ratio)]
      (three/LineBasicMaterial.
        (clj->js
          {:blending    three/AdditiveBlending
           :linewidth   2
           :color       (three/Color. r g b)
           :transparent false})))))

(def base-line-material (memoize base-line-material*))


(defn- active-link-material [col]
  (let [g->c-temp 90
        c->w-temp 80
        w->c-temp 50
        g->c-int  (- 100 g->c-temp)
        c->w-int  (- g->c-temp c->w-temp)
        w->c-int  (- c->w-temp w->c-temp)

        col->white-fn (between-colors col white)
        white->col-fn (between-colors white col)]

    (fn [t-base t]

      (let [ratio      (/ t-base render-utils/max-count)
            base-color (map #(* 255 %) (base->brighter-base ratio))

            base->col-fn (between-colors base-color col)
            col->base-fn (between-colors col base-color)

            [[r g b] ratio]
            (cond
              (> t g->c-temp)
              (let [t-norm (/ (- t g->c-temp) g->c-int)]
                [(base->col-fn t-norm)
                 (+ 2 t-norm)])

              (> t c->w-temp)
              (let [t-norm (/ (- t c->w-temp) c->w-int)]
                [(col->white-fn t-norm)
                 (- 4 (* 2 t-norm))])

              (> t w->c-temp)
              (let [t-norm (/ (- t w->c-temp) w->c-int)]
                [(white->col-fn t-norm) 2])

              :else
              (let [t-norm (/ t w->c-temp)]
                [(col->base-fn t-norm) 2]))]
       (three/LineBasicMaterial.
         (clj->js
           {:blending    three/AdditiveBlending
            :linewidth   ratio
            :color       (three/Color. r g b)
            :transparent false}))))))

(defn error-link-material [t-base t]
  (let [ratio      (/ t-base render-utils/max-count)
        base-color (map #(* 255 %) (base->brighter-base ratio))
        [r g b] ((between-colors error-red base-color) (/ (max 0 (- t 80)) 20))]
    (three/LineBasicMaterial.
      (clj->js
        {:blending    three/AdditiveBlending
         :linewidth   2
         :color       (three/Color. r g b)
         :transparent false}))))

(def active-link-material-error-red
  (memoize error-link-material))

(def active-link-material-red
  (memoize (active-link-material red)))

(def active-link-material-dark-red
  (memoize (active-link-material dark-red)))

(def active-link-material-dark-blue
  (memoize (active-link-material dark-blue)))

(def active-link-material-blue
  (memoize (active-link-material blue)))

(def active-link-material-yellow
  (memoize (active-link-material yellow)))



(def node-material
  (three/PointsMaterial.
   (clj->js {:vertexColors three/VertexColors :size 3 :sizeAttenuation false})))



(def node-material-active
  (three/PointsMaterial.
   (clj->js {:color 0xFF0000 :size 10 :sizeAttenuation false})))



