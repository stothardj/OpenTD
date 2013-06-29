(ns cake.spidereenest
  (:use [cake.pool :only [Pool]]
        [cake.spideree :only [spawn-spideree]]))

(defn- add-noise-to-path [path]
  (map (fn [[x y]]
         [(+ x -10 (rand-int 20))
          (+ y -10 (rand-int 20))])
       path))

(deftype SpidereeNest
    [n prob path]
  Pool
  (spawn-creep [this]
    (let [want-to-spawn (if (< (rand-int 1000) prob) 5 0)
          num-spawned (min want-to-spawn n)
          creep-left (- n num-spawned)
          new-creep (repeatedly num-spawned
                                #(let [[[cx cy]] path
                                       nx (+ cx -25 (rand-int 50))
                                       ny (+ cy -25 (rand-int 50))
                                       p (add-noise-to-path path)]
                                   (spawn-spideree nx ny p)))
          ]
      (if (zero? creep-left)
        {:creep new-creep}
        {:creep new-creep
         :pool (SpidereeNest. creep-left prob path)})
      )
    ))
