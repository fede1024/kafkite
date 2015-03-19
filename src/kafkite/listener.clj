(ns kafkite.listener
  (:require [clojure.core.async :refer [>!! <!! chan] :as async]
            [taoensso.timbre :as log]
            [clojure.java.io :as io])
  (:import [java.net ServerSocket]))

(defn msg-to-ch [socket ch]
  (let [reader (io/reader socket)]
    (loop [line (.readLine reader)]
      (when line
        (>!! ch [socket line])
        (recur (.readLine reader))))))

(defn socket-to-ch-limited [accept-ch line-ch & {:keys [threads] :or {threads 10}}]
  (doall
    (for [n (range threads)]
      (async/thread
        (loop [sock (<!! accept-ch)]
          (when sock
            (with-open [socket sock]
              (msg-to-ch socket line-ch))
            (recur (<!! accept-ch))))))))

(defn socket-to-ch [accept-ch line-ch]
  (async/thread
    (loop [sock (<!! accept-ch)]
      (async/thread
        (when sock
          (with-open [socket sock]
            (msg-to-ch socket line-ch))))
      (recur (<!! accept-ch)))))

(defn server [port line-ch]
  (let [server-sock (ServerSocket. port)]
    {:socket server-sock :thread
     (async/thread
       (try
         (while (not (.isClosed server-sock))
           (let [socket (.accept server-sock)
                 port (.getPort socket)
                 addr (.getHostAddress (.getInetAddress socket))]
             (log/debug "Connection accepted from" addr port)
             (async/thread
               (with-open [sock socket]
                 (msg-to-ch socket line-ch))
               (log/debug "Connection closed" addr port))))
         (catch java.net.SocketException e
           nil)
         (finally
           (.close server-sock)
           (log/info "Server stopped"))))}))

(defn stop-server! [server]
  (.close (:socket server)))
