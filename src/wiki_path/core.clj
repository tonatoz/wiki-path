(ns wiki-path.core
  (:require [org.httpkit.client :as http]
            [net.cgrand.enlive-html :as html]))

(def url "http://ru.m.wikipedia.org/wiki/SAP")

(def html-page
  (html/html-snippet
    (:body @(http/get url))))

(def raw-links
  (map (comp :href :attrs) (html/select html-page [:div.content :a])))

(def title
  (first (:content (first (html/select html-page [:h1#section_0])))))

(defn link-pred [link]
  (and (= "/wiki/" (subs link 0 6))
       (not (some #(= \. %) link))))

(def links
  (filter link-pred raw-links))
