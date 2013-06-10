(ns cake.hello
  (:use [cake.lasertower :only [LaserTower]]
        [cake.spawnling :only [Spawnling]]
        [cake.spawnlingpool :only [SpawnlingPool]]
        [cake.drawing :only [canvas]]
        )
  (:require [clojure.browser.event :as event]
            [cake.drawing :as drawing]
            [cake.creep :as creep]
            [cake.tower :as tower]
            [cake.pool :as pool]
            [cake.util :as util]
            [cake.line :as line]
            [cake.point :as point]
            [cake.animation :as animation]
            [cake.gamestate :as gamestate]
            )
  )

(def creep-path '((30 40) (70 90) (70 200) (300 200) (500 400) (600 500) (700 250) (600 100) (500 200)  ))

;; TODO: Use atoms for these
(def towers [(LaserTower. 200 300) (LaserTower. 100 400)])
(def creeps (atom [(Spawnling. 150 100 creep-path)
                   (Spawnling. 100 200 creep-path)
                   ]))

;; TODO: Make list
(def pool (atom (SpawnlingPool. 51 100 8 2 creep-path)))

(def mouse-pos (atom nil))

(defn relative-mouse-pos
  [ev]
  (let [rect (.getBoundingClientRect canvas)
        x (- (.-clientX ev) (.-left rect))
        y (- (.-clientY ev) (.-top rect))]
    [x y]))

(when canvas

  (event/listen canvas "click"
                (fn [ev]
                  (let [[x y] (relative-mouse-pos ev)]
                    (when-not (line/point-on-thick-path? [x y] creep-path 50)
                      (set! towers (cons (LaserTower. x y) towers)))
                    )))

  (event/listen canvas "mousemove"
                (fn [ev]
                  (let [p (relative-mouse-pos  ev)]
                    (reset! mouse-pos p))))

  (util/crashingInterval
   (fn []
     (drawing/clear-canvas)
     (drawing/draw-creep-path creep-path)
     (set! gamestate/time (.getTime (js/Date.)))
     (doseq [tower towers]
       (tower/draw tower))
     (doseq [creep @creeps]
       (creep/draw creep))
     
     (swap! creeps #(for [creep %
                          :let [new-creep (creep/move creep)]
                          :when (not (nil? new-creep))] new-creep))
     ;; "Kill" creeps which get close to towers
     (swap! creeps
            (fn [creeps]
              (filter
               (fn [creep]
                 (not-any?
                  (fn [tower]
                    (< (line/sq-point-to-point-dist
                        (point/get-point creep)
                        (point/get-point tower)) 2000))
                  towers))
               creeps)))

     (when @pool
       (let [r (pool/spawn-creep @pool)
             new-creep (:creep r)
             new-pool (:pool r)
             ]
         (swap! creeps #(concat % new-creep))
         (reset! pool new-pool)))
     )
   40)

  )
