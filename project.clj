(defproject scrap-world "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [cljs-ajax "0.8.4"]
                 [cnuernber/dtype-next "9.012"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [compojure "1.6.2"]
                 [garden "1.3.10"]
                 [hiccup "1.0.5"]
                 [http-kit "2.5.3"]
                 [instaparse "1.4.10"]
                 [jstrutz/hashids "1.0.1"]
                 [metosin/spec-tools "0.10.5"]
                 [missionary "b.26"]
                 [org.apache.logging.log4j/log4j-core "2.17.1"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.17.1"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.11.4"]
                 [org.clojure/core.async "1.5.648"]
                 [org.clojure/core.match "1.0.0"]
                 [org.clojure/tools.cli "1.0.206"]
                 [org.clojure/tools.logging "1.2.4"]
                 [pandect "1.0.2"]
                 [reagent "1.1.0"]
                 [ring "1.9.5"]
                 [ring/ring-json "0.5.1"]
                 [techascent/tech.ml.dataset "6.065"]
                 [thheller/shadow-cljs "2.17.4"]
                 ]

  :main ^:skip-aot scrap-world.core

  :target-path "target/%s"

  :source-paths ["src/cljc" "src/clj" "src/cljs"]

  :resource-paths ["cfg" "resources"]

  :clean-targets ^{:protect false} [:target-path "resources/public/js"]

  :aliases {"js-watch" ["run" "-m" "shadow.cljs.devtools.cli" "watch" "app"]
            "js-build" ["run" "-m" "shadow.cljs.devtools.cli" "release" "app"]
            "uber"     ["do" ["clean"] "js-build" ["uberjar"]]}

  :profiles {:uberjar {:aot        :all
                       :jvm-opts   ["-Dclojure.compiler.direct-linking=true"]
                       :prep-tasks ["clean" "compile" "js-build"]}})
