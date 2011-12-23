(ns foreclojure.datatable
  (:use [foreclojure.users :only [user-datatable-query]]
        [noir.core         :only [defpage]]
        [noir.response     :only [json]]))

(defpage "/datatable/users" {:as all}
  (json (user-datatable-query all)))
