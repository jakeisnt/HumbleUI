(ns examples.grid
  (:require
    [clojure.string :as str]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.font :as font]
    [io.github.humbleui.signal :as signal]
    [io.github.humbleui.typeface :as typeface]
    [io.github.humbleui.ui :as ui]))

(def face-bold
  (typeface/make-from-resource "io/github/humbleui/fonts/Inter-Bold.ttf"))

(let [[head & tail]
      (->> (slurp "dev/examples/currency.csv")
        (str/split-lines)
        (mapv #(str/split % #",")))]
  (def header head)
  (def currencies tail))

(def *state
  (signal/signal
    {:sort-col   0
     :sort-dir   :asc
     :currencies currencies}))

(defn on-click [i]
  (fn [e]
    (swap! *state
      (fn [{:keys [sort-col sort-dir] :as state}]
        (cond
          (not= i sort-col)
          {:sort-col   i
           :sort-dir   :asc
           :currencies (->> currencies (sort-by #(nth % i)))}
          
          (= :asc sort-dir)
          {:sort-col   i
           :sort-dir   :desc
           :currencies (->> currencies (sort-by #(nth % i)) reverse)}
          
          (= :desc sort-dir)
          {:sort-col   i
           :sort-dir   :asc
           :currencies (->> currencies (sort-by #(nth % i)))})))))

(defn ui []
  (let [{:keys [scale]}    ui/*ctx*
        font-bold          (font/make-with-cap-height face-bold (* scale 10))
        {:keys [currencies
                sort-col
                sort-dir]} @*state]
    [ui/vscrollbar
     [ui/halign {:position 0.5}
      [ui/grid {:cols (count header)
                :rows (inc (count currencies))}
       (concat
         (for [[th i] (core/zip header (range))]
           [ui/clickable
            {:on-click (on-click i)}
            [ui/padding {:padding 10}
             [ui/with-context {:font-ui font-bold}
              [ui/max-width
               {:probes [[ui/label (str th " ⏶")]
                         [ui/label (str th " ⏷")]]}
               [ui/halign {:position 0}
                [ui/label
                 (str th
                   (case (when (= i sort-col)
                           sort-dir)
                     :asc  " ⏶"
                     :desc " ⏷"
                     nil   ""))]]]]]])
         (for [row currencies
               s   row]
           [ui/padding {:padding 10}
            [ui/label s]]))]]]))
