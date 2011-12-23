(ns foreclojure.messages
  (:require [clojure.java.io :as io]))

(defn load-props [file]
  (into {} (doto (java.util.Properties.)
             (.load (io/reader (io/resource file))))))
 
(def err-msg-map  (load-props "error-messages.properties"))             
             
(defn err-msg [key & args]
      (apply format (cons (get err-msg-map key) args)))