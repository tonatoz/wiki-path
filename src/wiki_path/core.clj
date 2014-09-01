(ns wiki-path.core
  (:require [clojure.zip :as zip]
            [org.httpkit.client :as http]
            [net.cgrand.enlive-html :as html]))

(def url "/wiki/Split")
(def base-wiki-url "http://ru.m.wikipedia.org")

(defn get-title [url]
  "Return title of page by url. Need http request."
  (let [html (html/html-snippet (:body @(http/get (str base-wiki-url url))))]
    (first (:content (first (html/select html [:h1#section_0]))))))

(defn link-pred [link]
  "Rules for link to wiki document"
  (and (string? link)
       (< 6 (count link))
       (= "/wiki/" (subs link 0 6))
       (not (some #(contains? #{\. \:} %) link))))

(defn get-links [url]
  "Return all wiki links from url page"
  (let [html-page (html/html-snippet (:body @(http/get url)))]
    (filter link-pred (map (comp :href :attrs) (html/select html-page [:div.content :a])))))

(defn construct-path [node]
  "Return lazy seq of path of page titles from root to current node"
  (interpose " -> "
             (map (comp #(str "[" % "]") get-title)
                  (concat (zip/path node) [(zip/node node)]))))

(defn wiki-zipper [begin-url]
  (zip/zipper
    (constantly true)
    (fn [node] (seq (get-links (str base-wiki-url node))))
    nil
    url))

(defn bf-next [loc]
  "Breadth-First zipper walking"
  (or (zip/right loc)
      (if (zip/up loc)
        (if (-> loc zip/up zip/right)
          (-> loc zip/up zip/right zip/down)
          (-> loc zip/up zip/leftmost zip/down))
        (-> loc zip/down))))

(->> (wiki-zipper url)
      (iterate bf-next)
      (take 250)
      (map (comp get-title first)))