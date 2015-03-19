(ns kafkite.core
  (:gen-class)
  (:require [clj-kafka.producer :as producer] clj-kafka.consumer.zk clj-kafka.core
            [kafkite.listener :as listener]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [clj-kafka.producer :as producer]
            [clj-kafka.consumer.simple :as simple]
            [zookeeper :as zk]
            [cheshire.core :as json]
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

(defn parse-int [s]
  (try
    (when s (Integer/parseInt s))
    (catch java.lang.NumberFormatException e
      nil)))

(defn parse-float [s]
  (try
    (when s (Float/parseFloat s))
    (catch java.lang.NumberFormatException e
      nil)))

(defn parse-input-line [line]
  (let [[metric ts-str value-str] (str/split line #" ")
        ts (parse-int ts-str)
        value (parse-float value-str)] ;; TODO: is float enough?
    (if (and metric ts value)
      [metric ts value]
      nil)))

(defn store-metrics [line-ch producer]
  (async/thread
    (loop [input (<!! line-ch)]
      (when-let [[socket line] input]
        (log/trace line)
        (if-let [[metric ts value] (parse-input-line line)]
          (producer/send-message producer (message "raw" metric line))
          (log/warn "Wrong message format:" line)))
      (recur (<!! line-ch)))))

;(def server (listener/server 2000))
;(stop-server! server)
;(def ch (chan 100))
;(def threads (socket-to-ch (:chan server) ch :threads 10))
;(def server-atom (atom nil))

(defn start [port]
  (let [line-ch (chan 100)
        server (listener/server port line-ch)
        kafka-producer (producer/producer {"metadata.broker.list" "localhost:9092,localhost:9093"
                                           "serializer.class" "kafka.serializer.StringEncoder"
                                           "partitioner.class" "kafkite.kafka.partitioner" })]
    (store-metrics line-ch kafka-producer)
    (log/info "Started")
    server))

;(def server (start 2000))
;(listener/stop-server! server)

;(def line-ch (chan 100))
;(def server (listener/server 2000 line-ch))

(def kafka-producer (producer/producer {"metadata.broker.list" "localhost:9092,localhost:9093"
                                        "serializer.class" "kafka.serializer.StringEncoder"
                                        "partitioner.class" "kafkite.kafka.partitioner"}))

;(producer/send-message kafka-producer (message "test" "key" .getBytes "Ciaoooi"))

(def config {"zookeeper.connect" "localhost:2181"
             "group.id" "clj-kafka.consumer"
             "auto.offset.reset" "smallest" })

(def client (zk/connect "127.0.0.1:2181" :watcher (fn [event] (log/trace event))))

(zk/close client)

(get-zk-json client "/brokers/topics/raw")

(defn get-zk-json [client path]
  (try
    (when-let [s (:data (zk/data client path))]
      (json/parse-string (String. s)))
    (catch org.apache.zookeeper.KeeperException e
      nil)))

(defn get-topic-metadata [zk topic]
  (get-zk-json zk (str "/brokers/topics/" topic)))

(defn get-topic-partitions [zk topic]
  (into {} (map (fn [[k v]] [(parse-int k) v])
                (get (get-topic-metadata zk topic) "partitions"))))

(defn get-topic-preferred-replicas [zk topic]
  (into {} (map (fn [[k v]] [(parse-int k) (first v)])
                (get (get-topic-metadata zk topic) "partitions"))))

(defn get-preferred-partitions-of [zk broker-id topic]
  (map #(parse-int (first %))
       (filter (fn [[k v]]
                 (= (first v) broker-id))
               (get (get-topic-metadata zk topic) "partitions"))))

(get-topic-meta client "test1")
(get-topic-partitions client "test1")
(get-topic-preferred-replicas client "siii")
(get-preferred-partitions-of client 1 "siii")

(zk/children client "/brokers/topics")

