(ns foreclojure.register
  (:require [noir.session             :as   session]
            [noir.response            :as   response])
  (:import  [org.jasypt.util.password StrongPasswordEncryptor])
  (:use     [hiccup.form-helpers      :only [form-to text-field password-field]]
            [foreclojure.utils        :only [form-row assuming flash-error plausible-email?]]
            [foreclojure.template     :only [html-doc]]
            [foreclojure.messages     :only [err-msg]]
            [somnium.congomongo       :only [insert! fetch-one]]
            [noir.core                :only [defpage]]))

(defn register-page []
  (html-doc
   {:title "4clojure &ndash; Register"
    :content
    (list
     [:div.error (session/flash-get :error)]
     (form-to [:post "/register"]
              [:table
               (map form-row
                    [[text-field :user "Username (4-13 chars.)" (session/get :user)]
                     [password-field :pwd "Password (7+ chars.)"]
                     [password-field :repeat-pwd "Repeat Password"]
                     [text-field :email "Email" (session/get :email)]])
               [:tr
                [:td [:button {:type "submit"} "Register"]]]]))}))

(defn do-register [user pwd repeat-pwd email]
  (let [lower-user (.toLowerCase user)]
    (assuming [(nil? (fetch-one :users :where {:user lower-user}))
               (err-msg "settings.user-exists"),
               (< 3 (.length lower-user) 14)
               (err-msg "settings.uname-size"),
               (= lower-user
                  (first (re-seq #"[A-Za-z0-9_]+" lower-user)))
               (err-msg "settings.uname-alphanum")
               (< 6 (.length pwd))
               (err-msg "settings.pwd-size"),
               (= pwd repeat-pwd)
               (err-msg "settings.pwd-match"),
               (plausible-email? email)
               (err-msg "settings.email-invalid")
               (nil? (fetch-one :users :where {:email email}))
               (err-msg "settings.email-exists")]
      (do
        (insert! :users
                 {:user lower-user
                  :pwd (.encryptPassword (StrongPasswordEncryptor.) pwd)
                  :email email})
        (session/put! :user lower-user)
        (response/redirect "/"))
      (do
        (session/flash-put! :user user)
        (session/flash-put! :email email)
        (flash-error "/register" why)))))

(defpage "/register" []
  (register-page))

(defpage [:post "/register"] {:keys [user pwd repeat-pwd email]}
  (do-register user pwd repeat-pwd email))
