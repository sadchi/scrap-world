(defproject scrap-world "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [cljs-ajax "0.8.4"]
                 [cnuernber/dtype-next "9.012"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [com.github.seancorfield/next.jdbc "1.3.862"]
                 [criterium "0.4.6"]
                 [garden "1.3.10"]
                 [hiccup "1.0.5"]
                 [http-kit "2.5.3"]
                 [instaparse "1.4.10"]
                 [jstrutz/hashids "1.0.1"]
                 [metosin/jsonista "0.2.6"]
                 [metosin/malli "0.8.4"]
                 [metosin/reitit "0.5.18"]
                 [migratus "1.4.9"]
                 [missionary "b.26"]
                 [org.apache.logging.log4j/log4j-core "2.17.1"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.17.1"]
                 [org.babashka/sci "0.3.4"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.11.4"]
                 [org.clojure/core.async "1.5.648"]
                 [org.clojure/core.match "1.0.0"]
                 [org.clojure/tools.cli "1.0.206"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.postgresql/postgresql "42.5.4"]
                 [pandect "1.0.2"]
                 [reagent "1.1.0"]
                 [techascent/tech.ml.dataset "6.065"]
                 [thheller/shadow-cljs "2.17.4"]
                 ]

  :plugins [[migratus-lein "0.7.3"]]

  :main ^:skip-aot scrap-world.core

  :target-path "target/%s"

  :source-paths ["src/cljc" "src/clj" "src/cljs"]

  :resource-paths ["cfg" "resources" "migrations"]

  :clean-targets ^{:protect false} [:target-path "resources/public/js"]

  :aliases {"js-watch" ["run" "-m" "shadow.cljs.devtools.cli" "watch" "app"]
            "js-build" ["run" "-m" "shadow.cljs.devtools.cli" "release" "app"]
            "uber"     ["do" ["clean"] "js-build" ["uberjar"]]}

  :profiles {:uberjar {:aot        :all
                       :jvm-opts   ["-Dclojure.compiler.direct-linking=true"]
                       :prep-tasks ["clean" "compile" "js-build"]}}

  :migratus {:store         :database
             :migration-dir "migrations"
             :db            {:dbtype   "postgresql"
                             :dbname   "scrapworld"
                             :user     "master"
                             :password ~(get (System/getenv) "SCRAPWORLD_PSW")}}
  )
