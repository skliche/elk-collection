# elk-collection

This is a collection of my logstash configuration files and corresponding kibana dashboards.

Currently I am working with an outdated version of logstash and kibana. The files have been created for logstash-1.4.2 and the dashboards should work with kibana 3.

Kibana dashboards:
- *JVM Stats* is a kibana dasboard extending a base JVM monitoring template with BPM ressources such as BPM specific datasources and HTTP sessions.
- *BPM Template* is a template for monitoring access logs on WAS with specific queries for BPM
- *WebService Stats* is a template for monitoring request metrics. 

All the dashboards should work with the provided logstash config files.
