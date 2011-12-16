(ns foreclojure.core
  (:use     [foreclojure.config        :only [config]]
            [foreclojure.mongo         :only [prepare-mongo]]
            [foreclojure.ring-utils    :only [wrap-request-bindings]]
            [foreclojure.periodic      :only [schedule-task]]
            [ring.adapter.jetty        :only [run-jetty]]
            [ring.middleware.file-info :only [wrap-file-info]]
            [ring.middleware.gzip      :only [wrap-gzip]]
            [mongo-session.core        :only [mongo-session]]
            [foreclojure.static        :only [welcome-page]]
            [foreclojure.ring-utils    :only [static-url wrap-request-bindings]])
  (:require [foreclojure.config        :as   config]
            [foreclojure.ring          :as   ring]
            [noir.server               :as   server]
            [noir.core                 :as   noir]))

;; The 404 route has to come before the resources, otherwise resources
;; wont be found.
(noir/defpartial render-404 []
  [:head
   [:title "4clojure: Page not found"]]
  [:body
   [:div {:style "margin-left: auto; margin-right: auto; width: 300px;"}
    [:p {:style "text-align: center; width: 100%; margin-top: 45px; font-family: helvetica; color: gray; font-size: 25px;"} "404 &mdash; Page not found."]
    [:img {:style "margin-left: 18px;" :src (static-url "images/4clj-gus-confused-small.png")}]]])

(noir/post-route [:any "*"] {} (render-404))

;; We want for this specific route to have this middleware applied.
;; Noir doesn't currently have a way to do special resource stuff,
;; so we're hacking around that a bit.
(noir/compojure-route
 (-> (ring/resources "/")
     ring/wrap-url-as-file
     wrap-file-info
     ring/wrap-versioned-expiry))

(noir/defpage "/" [] (welcome-page))

(server/add-middleware wrap-request-bindings)
(server/add-middleware wrap-gzip)

(let [default-jetty-port 8080]
  (defn -main [& _]
    (prepare-mongo)
    (server/start
     (get config :jetty-port default-jetty-port)
     {:session-store (mongo-session :sessions)})))
