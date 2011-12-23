(ns foreclojure.api
  (:use [foreclojure.utils  :only [as-int]]
        [compojure.core     :only [routes GET]]
        [somnium.congomongo :only [fetch-one]]
        [useful.map         :only [update-each]]
        [noir.core          :only [defpage]]
        [noir.response      :only [json]]))

(defpage "/api/problem/:id" {:keys [id]}
  (when-let [problem (fetch-one :problems
                                :where {:_id (as-int id)
                                        :approved true})]
    (json
     (-> problem
         (dissoc :_id :approved)
         (update-each [:restricted :tags]
                      #(or % ()))))))
