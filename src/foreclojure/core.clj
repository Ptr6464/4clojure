(ns foreclojure.core
  (:use     [foreclojure.config        :only [config]]
            [foreclojure.mongo         :only [prepare-mongo]]
            [foreclojure.ring-utils    :only [wrap-request-bindings]]
            [foreclojure.periodic      :only [schedule-task]]
            [ring.adapter.jetty        :only [run-jetty]]
            [ring.middleware.file-info :only [wrap-file-info]]
            [ring.middleware.gzip      :only [wrap-gzip]]
            [mongo-session.core        :only [mongo-session]]
            [foreclojure.ring-utils    :only [static-url wrap-request-bindings]]
            [noir.core                 :only [defpartial]]
            [noir.statuses             :only [set-page!]])
  (:require [foreclojure.config        :as   config]
            [foreclojure.ring          :as   ring]
            [noir.server               :as   server]
            ;; Views. Eventually, views and models will be separated, in which case we'll
            ;; be able to use noir's loading capabilities. Until then, we'll just require
            ;; them.
            foreclojure.static
            foreclojure.login
            foreclojure.register
            foreclojure.settings
            foreclojure.golf
            foreclojure.version))

(defpartial render-404 []
  [:head
   [:title "4clojure: Page not found"]]
  [:body
   [:div {:style "margin-left: auto; margin-right: auto; width: 300px;"}
    [:p {:style "text-align: center; width: 100%; margin-top: 45px; font-family: helvetica; color: gray; font-size: 25px;"} "404 &mdash; Page not found."]
    [:img {:style "margin-left: 18px;" :src (static-url "images/4clj-gus-confused-small.png")}]]])

(set-page! 404 (render-404))

(server/wrap-route :resources ring/wrap-url-as-file)
(server/wrap-route :resources wrap-file-info)
(server/wrap-route :resources ring/wrap-versioned-expiry)

(server/add-middleware wrap-request-bindings)
(server/add-middleware wrap-gzip)

(defn -main [& _]
  (prepare-mongo)
  (server/start
   (get config :jetty-port 8080)
   {:session-store (mongo-session :sessions)}))
