(ns foreclojure.version
  (:use [foreclojure.template :only [html-doc]]
        [foreclojure.config   :only [repo-url]]
        [foreclojure.git      :only [sha tag]]
        [noir.core            :only [defpage]]))

(defn version []
  (html-doc
   {:title "About/version"
    :content
    (if tag
      [:p
       [:a {:href (str repo-url "/tree/" sha)} tag]]
      [:p "No git repository found"])}))

(defpage "/about/version" []
  (version))
