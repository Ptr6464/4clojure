(ns foreclojure.ring
  (:require [clojure.java.io           :as   io]
            [clojure.string            :as   s]
            [cheshire.core             :as   json])
  (:import  [java.net                  URL])
  (:use     [hiccup.core               :only [html]]
            [foreclojure.version-utils :only [strip-version-number]]
            [foreclojure.ring-utils    :only [get-host static-url]]
            [useful.debug              :only [?]]
            [ring.util.response        :only [response]]))

(defn wrap-url-as-file [handler]
  (fn [request]
    (when-let [{body :body :as resp} (handler request)]
      (if (and (instance? URL body)
               (= "file" (.getProtocol ^URL body)))
        (update-in resp [:body] io/as-file)
        resp))))

(defn wrap-strip-trailing-slash [handler]
  (fn [request]
    (handler (update-in request [:uri] s/replace #"(?<=.)/$" ""))))

(defn wrap-versioned-expiry [handler]
  (fn [request]
    (when-let [resp (handler
                     (update-in request [:uri] strip-version-number))]
      (assoc-in resp [:headers "Cache-control"]
                "public, max-age=31536000"))))

(defn wrap-debug [handler label]
  (fn [request]
    (println "In" label)
    (? (handler (? request)))))

(defn split-hosts [host-handlers]
  (let [default (:default host-handlers)]
    (fn [request]
      (let [host (get-host request)
            handler (or (host-handlers host) default)]
        (handler request)))))