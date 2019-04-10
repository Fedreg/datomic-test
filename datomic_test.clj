(ns datomic-test
  (:require
   [datomic.client.api :as d]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Datomic Config
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def cfg {:server-type :peer-server
          :access-key "myaccesskey"
          :secret "mysecret"
          :endpoint "localhost:8998"})

(def client (d/client cfg))
(def conn (d/connect client {:db-name "hello"}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schemas
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def movie-schema
  [{:db/ident :movie/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The title of the movie"}

   {:db/ident :movie/genre
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The genre of the movie"}

   {:db/ident :movie/release-year
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "The year the movie was released in theaters"}])

(d/transact conn {:tx-data movie-schema})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Storing Data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def first-movies
  [{:movie/title "The Goonies"
    :movie/genre "action/adventure"
    :movie/release-year 1985}
   {:movie/title "Commando"
    :movie/genre "action/adventure"
    :movie/release-year 1985}
   {:movie/title "Repo Man"
    :movie/genre "punk dystopia"
    :movie/release-year 1984}])

(d/transact conn {:tx-data first-movies})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Querying Data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def db (d/db conn))

;; To find entity
(def all-movies-q
  '[:find ?e
    :where [?e :movie/title]])

(d/q all-movies-q db)

;; To find titles
(def all-movie-titles
  '[:find ?movie-title
    :where [_ :movie/title ?movie-title]])

(d/q all-movie-titles db)

;; Year
(d/q
'[:find ?movie-year
  :where [_ :movie/release-year ?movie-year]]
db)

(d/q
 '[:find ?title
   :where [?e :movie/title ?title]
          [?e :movie/release-year 1985]]
 db)

(d/q
 '[:find ?title ?year ?genre
   :where
   [?e :movie/title ?title]
   [?e :movie/release-year ?year]
   [?e :movie/genre ?genre]
   [?e :movie/title "Repo Man"]]
 db)

(def commando-id
  (ffirst (d/q '[:find ?e
                 :where [?e :movie/title "Commando"]]
               db)))

(d/transact conn {:tx-data [{:db/id commando-id :movie/genre "future governor"}]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Inspect Old Values
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def old-db (d/as-of db 1004))

(def hdb (d/history db))

(d/q '[:find ?genre
       :where [?e :movie/title "Commando"] 
       [?e :movie/genre ?genre]] 
     hdb)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Full Tutorial
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
