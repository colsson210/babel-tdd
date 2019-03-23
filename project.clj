(defproject babel-tdd "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/clojurescript "1.10.520"]
                 [lein-doo "0.1.11"]
                 [devcards "0.2.6"]
                 [cljsjs/babylon "3.3.0-0"]
                 [binaryage/oops "0.7.0"]
                 [cljs-http "0.1.46"]]
  :plugins [[lein-figwheel "0.5.18"]
            [lein-doo "0.1.11"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]
  :source-paths ["src"]
  :cljsbuild {
              :test-commands {"test" ["lein" "doo" "phantom" "test" "once"]}
              :builds [
                       {:id "dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {
                                   :main babel-tdd.core
                                   :asset-path "cljs/out"
                                   :output-to "resources/public/cljs/main.js"
                                   :output-dir "resources/public/cljs/out"
                                   :source-map-timestamp true
                                   }}
                       {:id "devcards-test"
                        :source-paths ["src" "test"]
                        :figwheel {:devcards true}
                        :compiler {:main runners.browser
                                   :optimizations :none
                                   :asset-path "cljs/tests/out"
                                   :output-dir "resources/public/cljs/tests/out"
                                   :output-to "resources/public/cljs/tests/all-tests.js"
                                   :source-map-timestamp true}}
                       {:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:main runners.doo
                                   :optimizations :none
                                   :output-to "resources/public/cljs/tests/all-tests.js"}}]
              }
  :clean-targets ^{:protect false} [:target-path "out" "resources/public/cljs"]
  :figwheel  { :css-dirs ["resources/public/css"] })
