(defproject aduser "0.1.0-SNAPSHOT"
  :description "Runs a Jetty web server to allow reset of AD user passwords"
  :url "https://github.com/viperscape/aduser-reset/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.apache.commons/commons-exec "1.1"]
                 [commons-codec/commons-codec "1.8"]
                 [org.clojars.hozumi/clj-commons-exec "1.0.6"]
                 [compojure "1.1.5"]
                 [com.draines/postal "1.10.2"]
                 [ring "1.1.8"]
                 [http-kit "2.1.2"]]
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler com.aduser.core/app}
  :main com.aduser.core
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
