(ns wiki-path.core
  (:require [clojure.zip :as zip]
            [org.httpkit.client :as http]
            [net.cgrand.enlive-html :as html]))

(def url "/wiki/SAP")

(defn get-title [url]
  (let [html (html/html-snippet (:body @(http/get url)))]
    (first (:content (first (html/select html [:h1#section_0]))))))

(defn link-pred [link]
  (and (< 6 (count link))
       (= "/wiki/" (subs link 0 6))
       (not (some #(contains? #{\. \:} %) link))))

(defn get-links [url]
  (let [html-page (html/html-snippet (:body @(http/get url)))]
    (filter link-pred (map (comp :href :attrs) (html/select html-page [:div.content :a])))))

(def wiki-zipper
  (zip/zipper
    (constantly true)
    (fn [node] (seq (get-links (str "http://ru.m.wikipedia.org" node))))
    nil
    url))

(get-title
  (str "http://ru.m.wikipedia.org"
       (-> wiki-zipper
           (zip/down)
           (zip/right)
           (zip/right)
           (zip/down)
           (zip/node))))