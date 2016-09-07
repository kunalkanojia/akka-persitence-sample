## Akka persistence CRUD example with cassandra journal

Uses exact same models and aggregators defined in this blog about eventuate - [http://kkanojia.me/blog/event-sourcing-part-1](http://kkanojia.me/blog/event-sourcing-part-1)

Uses Persistent Queries to implement the query side.

To run the example you need to have a cassandra instance running. 
Change the casandra `contact-points` configuration parameter to point to your local cassandra. 






