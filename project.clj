(defproject kafkite "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [http-kit "2.1.18"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [clj-kafka "0.2.8-0.8.1.1"]
                 [zookeeper-clj "0.9.1"]
                 [com.taoensso/timbre "3.4.0"] ]
  :main ^:skip-aot kafkite.core
  :aot [kafkite.kafka.partitioner]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
