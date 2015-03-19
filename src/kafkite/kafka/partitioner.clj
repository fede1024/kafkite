(ns kafkite.kafka.partitioner
  (:gen-class
    :name kafkite.kafka.partitioner
    :implements [kafka.producer.Partitioner]
    :init init
    :state state
    :constructors {[kafka.utils.VerifiableProperties] []}))

(defn -init [properties]
  [[] (ref properties)])

(defn -partition [this k part-num]
  (mod (hash k) part-num))
