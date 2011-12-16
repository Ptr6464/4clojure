(defproject foreclojure "1.7.0"
  :description "4clojure - a website for learning Clojure"
  :dependencies [[clojure "1.3.0"]
                 [noir "1.2.1"]
                 [org.clojars.ninjudd/data.xml "0.0.1-SNAPSHOT"]
                 [clojail "0.5.1"]
                 [congomongo "0.1.7"]
                 [org.jasypt/jasypt "1.7"]
                 [cheshire "2.0.2"]
                 [useful "0.7.5"]
                 [amalloy/ring-gzip-middleware "[0.1.0,)"]
                 [amalloy/mongo-session "0.0.1"]
                 [tentacles "0.1.3"]
                 [clj-config "0.2.0"]
                 [incanter/incanter-core "1.3.0-SNAPSHOT"]
                 [incanter/incanter-charts "1.3.0-SNAPSHOT"]
                 [commons-lang "2.6"]
                 [org.apache.commons/commons-email "1.2"]]
  :dev-dependencies [[midje "1.3.0"]]
  :checksum-deps true
  :main foreclojure.core
  :ring {:handler foreclojure.core/app
         :init foreclojure.mongo/prepare-mongo})
