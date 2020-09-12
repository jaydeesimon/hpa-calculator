(ns ^:figwheel-hooks bumpy-tip.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

(defonce app-state (atom {:current-replicas 1
                          :desired-replicas 1
                          :current-metric 1
                          :desired-metric 1}))

(defn calc-desired-replicas [current-replicas current-metric desired-metric]
  (js/Math.ceil (* current-replicas (/ current-metric desired-metric))))

(defn slider [title property value min max]
  [:div
   [:p (str title ": " value)]
   [:input {:type "range" :value value :min min :max max
            :style {:width "100%"}
            :on-change (fn [e]
                         (let [new-value (js/parseInt (.. e -target -value))]
                           (swap! app-state
                                  (fn [app-state]
                                    (let [{:keys [:current-replicas :current-metric :desired-metric]}
                                          (assoc app-state property new-value)]
                                      (-> (assoc
                                            app-state
                                            :desired-replicas
                                            (calc-desired-replicas
                                              current-replicas
                                              current-metric
                                              desired-metric))
                                          (assoc property new-value)))))))}]])

(defn container []
  [:section {:class "mw5 mw7-ns center bg-light-gray pa3 ph5-ns"}
   [slider "Desired Replicas" :desired-replicas (:desired-replicas @app-state) 1 20]
   [slider "Current Replicas" :current-replicas (:current-replicas @app-state) 1 20]
   [slider "Current Requests Per Second" :current-metric (:current-metric @app-state) 0 1000]
   [slider "Desired Average Requests Per Second" :desired-metric (:desired-metric @app-state) 0 1000]])

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
  (swap! app-state update-in [:__figwheel_counter] inc)
)
