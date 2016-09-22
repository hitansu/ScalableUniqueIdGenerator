# ScalableUniqueIdGenerator

This is just a POC to demonstate how to use Ignite to generate unique ids.
This needs a database configuration as it saves the last generated id to db so that later it can start the id from
that point when node goes down.

Step to follow to run:
-----------------------
Provide the your DB details ConnectionManager
Start the Ignite node as server by executing ignite.bat or running a simple java app
Start an Ignite node as client by setting the client mode
Run the application.


NOTE: Somehow if the first server node is stopped the Idgeneration stops rather it should work.Working on that :)
