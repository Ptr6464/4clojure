(ns foreclojure.social
  (:require [tentacles.gists      :as   gist]
            [noir.session         :as   session])
  (:import  [java.net             URLEncoder])
  (:use     [foreclojure.utils    :only [escape-html]]
            [foreclojure.template :only [html-doc]]
            [compojure.core       :only [defroutes GET]]
            [hiccup.page-helpers  :only [link-to]]
            [somnium.congomongo   :only [fetch-one]]
            [noir.core            :only [defpage]]))

(defn throttled
  "Create a version of a function which 'refuses' to be called too
  frequently. If it has successfully been called in the last N milliseconds,
  calls to it will return nil; if no calls have succeeded in that period, args
  will be passed along to the base function."
  [f ms-period]
  (let [tracker (atom {:last-sent 0})]
    (fn [& args]
      (when (:accepted (swap! tracker
                              (fn [{:keys [last-sent]}]
                                (let [now (System/currentTimeMillis)
                                      ok (< ms-period (- now last-sent))]
                                  {:accepted ok
                                   :last-sent (if ok now last-sent)}))))
        (apply f args)))))

(def clojure-hashtag (throttled (constantly "#clojure ")
                                (* 1000 60 60))) ; hourly

(defn tweet-link [id status & [anchor-text]]
  (str "<a href=\"http://twitter.com/share?"
       "text=" (URLEncoder/encode status)
       "&url=" (URLEncoder/encode
                (str "https://4clojure.com/problem/" id))
       "&related=4clojure"
       "\">"
       (or anchor-text "Twitter")
       "</a>"))

(defn get-problem-title [id]
  (:title
   (fetch-one :problems
              :only [:title]
              :where {:_id id})))

(defn gist!
  "Create a new gist containing a user's solution to a problem and
  return its url."
  [user-name problem-num solution]
  (let [[user-name possessive] (if user-name
                                 [user-name "'s"]
                                 ["anonymous" nil])
        name (get-problem-title problem-num)
        filename (str user-name "-4clojure-solution" problem-num ".clj")
        text (str ";; " user-name possessive " solution to " name "\n"
                  ";; https://4clojure.com/problem/" problem-num
                  "\n\n"
                  solution)]
    (try
      (:html_url (gist/create-gist {filename text}))
      (catch Throwable _))))

(defn tweet-solution [id gist-url & [link-text]]
  (let [status-msg (str "Check out how I solved "
                        (let [title (get-problem-title id)]
                          (if (> (count title) 35)
                            (str "problem " id)
                            (str "\"" title "\"")))
                        " on #4clojure " (clojure-hashtag) gist-url)]
    (tweet-link id status-msg link-text)))

(defn share-page []
  (html-doc
   {:title "Share your code!"
    :content
    (if-let [[id code] (session/get :code)]
      (let [user (session/get :user)
            gist-url (gist! user id code)
            gist-link (if gist-url
                        [:div {:id "shared-code-box"}
                         [:div.code
                          [:h3 "Your Solution"]
                          [:pre {:class "brush: clojure;gutter: false;toolbar: false;light: true"} (escape-html code)]]
                         [:br]
                         [:div.share
                          "Share this " (link-to gist-url "solution")
                          " on " (tweet-solution id gist-url) "?"]]
                        [:div.error
                         "Failed to create gist of your solution"])]
        gist-link)
      [:div.error
       "Sorry...I don't remember you solving anything recently!"])}))


(defpage "/share/code" [] (share-page))
