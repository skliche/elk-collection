#!/bin/sh
vmstat 60 | perl -nle 'BEGIN {$|++} print scalar (localtime), " ", $_' | /opt/IBM/elastic/logstash-2.0.0/bin/logstash -f readAndDeliverVmstatWithTimestamp.conf
