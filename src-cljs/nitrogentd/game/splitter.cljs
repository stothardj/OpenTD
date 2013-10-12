(ns nitrogentd.game.splitter
  (:use [nitrogentd.game.creep :only [Creep]]
        [nitrogentd.game.drawing :only [ctx]]
        [nitrogentd.game.point :only [Point]]
        [nitrogentd.game.gamestate :only [time]]
        [nitrogentd.game.numberanimation :only [NumberAnimation]]
        [nitrogentd.game.creepstats :only [map->CreepStats]]
        )
  (:require [nitrogentd.game.util :as util]
            [nitrogentd.game.drawing :as drawing]
            [nitrogentd.game.creep :as creep]
            [nitrogentd.game.statuseffect :as statuseffect]
            )
  )

(def stats (map->CreepStats {:health 200 :speed 1}))

(def max-incarnation 3)

(defrecord Splitter [x y health path status-effects incarnation]
  Creep
  (draw [this]
    (set! (.-fillStyle ctx) "rgb(255,0,0)")
    (drawing/draw-at #(.fillRect ctx -5 -5 10 10) x y))
  (move [this]
    (when-not (empty? path)
      (let [current-stats (statuseffect/apply-effects status-effects stats)
            move-speed (:speed current-stats)
            {:keys [x y path]} (creep/move-along-path [x y] move-speed 100 path)]
        (Splitter. x y health path status-effects incarnation))))
  (damage [this force]
    (let [new-health (- health force)]
      (if (pos? new-health)
        {:creeps [(Splitter. x y new-health path status-effects incarnation)]
         :animations [(NumberAnimation. time force x y)]}
        (when (< incarnation max-incarnation)
          {:creeps [(Splitter. (+ x 10) (+ y 10) (:health stats) path [] (inc incarnation))
                    (Splitter. (- x 10) (- y 10) (:health stats) path [] (inc incarnation))]
           :animations [(NumberAnimation. time health x y)]}))))
  (add-effect [this effect]
    (let [new-effects (statuseffect/add-effect effect status-effects)]
      {:creeps [(Splitter. x y health path new-effects [])]}))
  Point
  (get-point [this] [x y]))

(defn spawn-splitter
  "Create and return Splitter with given params."
  [x y path]
  (let [health (:health stats)
        incarnation 0]
    (Splitter. x y health path [] incarnation)))
