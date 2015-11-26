#!/bin/sh

# In an infinite loop, poll for gc.log.* files, parse then with the gclog.Main program, filter already seen lines, and deliver to logstash

while(true) do 
	./runGCLog.sh -node TEST.AppCluster.MycompanyNode01 /opt/IBM/WebSphere/AppServer/profiles/Node01/logs/TEST.AppCluster.MycompanyNode01/gc.log.*  | ./runUnique.sh /home/monitoring/uniqueGC.idx | ./logstash-2.0.0/bin/logstash -f readGCLog.conf

	sleep 120
done
