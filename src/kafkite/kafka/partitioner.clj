(ns kafkite.kafka.partitioner
  (:gen-class
    :name kafkite.kafka.partitioner
    :init init
    :state state
    :constructors {[kafka.utils.VerifiableProperties] []}
    :implements [kafka.producer.Partitioner]))

(defn -init [properties]
  (println 'prop properties)
  [[] (ref properties)])

(defn -partition [this k part-num]
  (println '>> k part-num)
  0)
