(ns com.aduser.core
  (:gen-class)
  (:use compojure.core
        postal.core
        org.httpkit.server)
  (:require [clj-commons-exec :as exec]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [ring.middleware.reload :as reload])

  (:import (org.apache.commons.codec.digest DigestUtils)
           (org.apache.commons.codec.binary Base64)))

(require '[ring.util.codec :as r-codec])

(declare main)

;hold the tokens in memory
(defonce user-tokens (atom{}))


(defn add-token! [user token pass]
  (swap! user-tokens conj {(keyword user) {:token token, :pass pass}}))

(defn remove-token! [user]
  (reset! user-tokens (dissoc (into {} @user-tokens) (keyword user))))

;Base64/encodeInteger?
(defn gen-token []
  (Base64/encodeBase64URLSafeString (DigestUtils/sha256 (str (java.util.UUID/randomUUID)))))

(defn eat-token [token]
  (Base64/decodeBase64 token))

(defn user-token-valid? [user token]
 (if-let [_ (@user-tokens (keyword user))]
  (= (_ :token) token)))


(defn get-info [adinfo tag]
 (let [i (clojure.string/split
  (let [s (.indexOf adinfo tag)]
    (let [s2 (.indexOf adinfo "\r\n" s)]
      (subs adinfo (+ s (count (into [] tag))) s2)))  #"\s+")] (vec (next i))))

(defn str-contains? [text tag]
  (> (.indexOf text tag) -1))

 (defn gen-email-body [params]
  (let [[f l] (get-info (params :info) "Full Name")]
   (str "Hi " f ", to reset your account password please click
        <a href=\"http://localhost:3000/" (params :user) "/reset/" (params :token) "\">this link</a>
        </br></br>Your new password will be: " (params :pass))))

(defn sendmail [params]
  (postal.core/send-message
          ^{:host "email-server-hostname-or-ip"
            :user "email-server-login-name"
            :pass "thespecialpassword"}
              {:from "resetpw-no-reply@domain.com"
               :to (str (params :user) "@domain.com");(get params :user)
               :subject "Domain Password Reset"
               :body [:alternative
                      {:type "text/html"
                       :content
                        (if (get params :body)
                         (get params :body)
                         (gen-email-body params))}
                      ]}))


(defn htmlify [s]
  (clojure.string/replace s
    #"\r|\n" {"\r" "<br>","\n" "</br>"}))

(defn get-user [user]
  "returns user info, nil if does not complete"
  (let [info (let [cmd (str "net user " user " /domain")]
     (get @(clj-commons-exec/sh ["cmd" "/C" cmd]) :out))]
   (if (str-contains? info "completed successfully") info)))

(defn set-user! [user pass]
  "resets the user account with the password specified"
  (let [info (let [cmd (str "net user " user " " pass " /domain")]
     (get @(clj-commons-exec/sh ["cmd" "/C" cmd]) :out))]
    (if (str-contains? info "completed successfully") info)))


(defn send-successful? [res]
  (identical? (get res :error) :SUCCESS))


(defn req-reset-pw! [params]
  "initiates reset of user domain password, sends email confirmation first, notifies admin as well"
 (let [user (params :user)]
 (let [pass (r-codec/url-decode (params :pass))] ;decode this! it may have !@#$%^&*
 (if-let [userinfo (get-user user)]
  (if (= (first(get-info userinfo "User may change password")) "Yes") ;this would also return false on bad usernames submitted
   (let [token (gen-token)]
    (if (send-successful? (sendmail {:user user, :info userinfo, :token token, :pass pass}))
      (do
        (add-token! user token pass)
        (if (sendmail {:user "itdept-admin", :body (str "User " user " Initated Password Reset \r\n\r\n" (htmlify userinfo))})
          (str "Success")
          (str "Error")))
      (str "Error")))
   (str "User cannot change password"))
   (str "Error")))))

(defn reset-pw! [user token]
  (if (user-token-valid? user token)
    (let [pass (get (get @user-tokens (keyword user)) :pass)]
     (if (str-contains? (set-user! user pass) "completed successfully")
      (do
        (remove-token! user)
        (str "<h1><b>Resetting " user " with password " pass "</b></h1>"))
       (str "<h1><b>Unable to reset user password!</b></h1>")))
    (str "<h1><b>Token expired, please reapply for a password-reset!</b></h1>")))

(defroutes app-routes
  (GET "/" [] (resp/redirect "/index.htm"))
  (GET "/get/:user" [user] (get-user user))
  (GET "/debug" {params :params} (if (= (params :pass) "secretpasswordhere") (str "registered tokens: " @user-tokens)))
  (GET "/debug/reset" {params :params} (if (= (params :pass) "secretpasswordhere") (do (reset! user-tokens {})(str "resetting tokens: " @user-tokens))))
  (GET "/:user/reset" {params :params} (req-reset-pw! params))
  ;(GET "/:user" {params :params} (str (r-codec/url-decode (params :pass))))
  (GET "/reset" {params :params} (req-reset-pw! params)) ;alt-form
  (GET "/:user/reset/:token" [user token] (reset-pw! user token))
  (route/resources "/")
  (route/not-found "Not Found!"))

(def app
  (compojure.handler/site app-routes))

(use 'ring.adapter.jetty)

(defn -main [& args] (run-jetty #'app {:port 3000}));run-jetty


