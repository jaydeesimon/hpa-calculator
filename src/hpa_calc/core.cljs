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

(defn replicas-color-phrase [current-replicas desired-replicas]
  (cond
    (= current-replicas desired-replicas)
    ["green" "Optimal"]

    (< current-replicas desired-replicas)
    ["red" "Underprovisioned"]

    :else
    ["orange" "Overprovisioned"]))

(defn container []
  [:section {:class "mw5 mw7-ns center bg-light-gray pa3 ph5-ns"}
   [:h3 {:class "tc"}
    (let [current-replicas (:current-replicas @variables)
          desired-replicas (:desired-replicas @variables)
          [color phrase] (replicas-color-phrase current-replicas desired-replicas)]
      [:span {:class color} phrase])]
   [:h3 {:class "tc"} (str "Desired Replicas: " (:desired-replicas @variables))]
   [:h3 {:class "tc"} (str "Current Average Request Count: " (:current-average-metric @variables))]
   [slider "Current Replicas" :current-replicas 1 20]
   [slider "Current Request Count" :current-metric 0 300]
   [slider "Desired Average Request Count" :desired-average-metric 0 100]
   [:div {:class "ph0 courier f6 pa3"}
    [:h4 {:class "pa0 f6 ph0"} "Notes"]
    [:ul {:class "ph3"}
     [:li {:class "ph0 pa2"} "The above is based on the " [:a {:target "_blank" :href "https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/#algorithm-details"} "HPA formula"] "."]
     [:li {:class "ph0 pa2"} "Current Average Request Count = ceil(Current Request Count / Current Replicas)"]
     [:li {:class "ph0 pa2"} "Desired Average Request Count is the target average value, the amount of traffic each replica will handle."]
     [:li {:class "ph0 pa2"} "Current Request Count is the raw metric value pulled from Datadog."]]]])

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
