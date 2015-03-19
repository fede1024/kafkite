(ns kafkite.core
  (:gen-class)
  (:require [clj-kafka.producer :as producer] clj-kafka.consumer.zk clj-kafka.core
            [kafkite.listener :as listener]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [clj-kafka.producer :as producer]
            [clojure.core.async :refer [>!! <!! chan] :as async]))

(log/merge-config!
  {:timestamp-pattern "yyyy-MMM-dd HH:mm:ss"
   :fmt-output-fn (fn [{:keys [level throwable message timestamp hostname ns]}
                       & [{:keys [nofonts?] :as appender-fmt-output-opts}]]
                    ;; <timestamp> <LEVEL> [<ns>] - <message> <throwable>
                    (format "%s %s [%s] - %s%s"
                            timestamp (-> level name str/upper-case) ns (or message "")
                            (or (log/stacktrace throwable "\n" (when nofonts? {})) ""))) })

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(use 'clj-kafka.producer)

(def p (producer {"metadata.broker.list" "localhost:9092"
                  "serializer.class" "kafka.serializer.DefaultEncoder"
                  "partitioner.class" "kafka.producer.DefaultPartitioner"}))

;(send-message p (message "test" (.getBytes "this is my message")))

(use 'clj-kafka.consumer.zk)
(use 'clj-kafka.core)

(def config {"zookeeper.connect" "localhost:2181"
             "group.id" "clj-kafka.consumer"
             "auto.offset.reset" "smallest"
             "auto.commit.enable" "false"})

(defn start-reader [ch]
  (async/thread
    (loop []
      (when-let [sock (<!! ch)]
        (println '> sock)
        (recur)))))

(def m (array-map))

(let [m (array-map)
      p (assoc m "metric" {:ts 3 :a 1}
             "metric1" {:ts 2 :a 1}
             "metric2" {:ts 5 :a 1})
      q (assoc p "metric3" {:ts 3 :a 'lol}) ]
  (last (for [[k v] q]
           [k v]
           )))

(defmethod print-method clojure.lang.PersistentQueue
  [q, w]

  (print-method '<- w)
  (print-method (seq q) w)
  (print-method '-< w))

(def q clojure.lang.PersistentQueue/EMPTY)

(println q)

(def q1 (conj q :a :b :c))
(def q2 (conj q1 1 2 3))

(defn store-metrics [line-ch producer]
  (async/thread
    (loop [input (<!! line-ch)]
      (when-let [[socket line] input]
        (log/trace line)
        (send-message p (message "raw" (.getBytes line)))
        (recur (<!! line-ch))))))

;(def server (listener/server 2000))
;(stop-server! server)
;(def ch (chan 100))
;(def threads (socket-to-ch (:chan server) ch :threads 10))
;(def server-atom (atom nil))

(defn start [port]
  (let [line-ch (chan 100)
        server (listener/server port line-ch)
        kafka-producer (producer/producer {"metadata.broker.list" "localhost:9092,localhost:9093"
                                           "serializer.class" "kafka.serializer.DefaultEncoder"
                                           "partitioner.class" "kafkite.kafka.partitioner"
                                           ;"partitioner.class" "kafka.producer.DefaultPartitioner"
                                           })]
    (store-metrics line-ch kafka-producer)
    (log/info "Started")
    server))

(def server (start 2000))
(listener/stop-server! server)

;(def line-ch (chan 100))
;(def server (listener/server 2000 line-ch))

(def kafka-producer (producer/producer {"metadata.broker.list" "localhost:9092,localhost:9093"
                                        "serializer.class" "kafka.serializer.StringEncoder"
                                        "partitioner.class" "kafkite.kafka.partitioner"}))

(send-message kafka-producer (message "test" "key" .getBytes "Ciaoooi"))


