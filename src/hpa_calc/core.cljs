(ns ^:figwheel-hooks hpa-calc.core
  (:require
    [goog.dom :as gdom]
    [reagent.core :as reagent :refer [atom]]
    [reagent.dom :as rdom]))

(defonce variables (atom {:current-replicas 1
                          :desired-replicas 1
                          :current-average-metric 1
                          :current-metric 1
                          :desired-average-metric 1}))

(defn calc-desired-replicas [{:keys [:current-replicas
                                     :current-metric
                                     :desired-average-metric] :as variables}]
  (let [current-average-metric (js/Math.ceil (/ current-metric current-replicas))]
    (-> (assoc
          variables
          :desired-replicas
          (js/Math.ceil (* current-replicas (/ current-average-metric desired-average-metric))))
        (assoc :current-average-metric current-average-metric))))

(defn slider [title property min max]
  (let [value (property @variables)]
    [:div
     [:p (str title ": " value)]
     [:input {:type "range" :value value :min min :max max
              :style {:width "100%"}
              :on-change (fn [e]
                           (let [new-value (js/parseInt (.. e -target -value))]
                             (swap! variables
                                    (fn [app-state]
                                      (-> (assoc app-state property new-value)
                                          calc-desired-replicas)))))}]]))

(defn container []
  [:section {:class "mw5 mw7-ns center bg-light-gray pa3 ph5-ns"}
   [:h3 {:class "tc"} (str "Desired Replicas: " (:desired-replicas @variables))]
   [:h3 {:class "tc"} (str "Current Average Request Count: " (:current-average-metric @variables))]
   #_[slider "Desired Replicas" :desired-replicas 1 20]
   [slider "Current Replicas" :current-replicas 1 20]
   [slider "Current Request Count" :current-metric 0 1000]
   [slider "Desired Average Request Count" :desired-average-metric 0 1000]])

(defn mount [el]
  (rdom/render
    [:div {:class "sans-serif"}
     [container]]
    el))

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element)
  (swap! variables update-in [:__figwheel_counter] inc))
