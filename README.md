# ODPSourceConnector
## Overview
This SAP ODP connector is a Kafka Connector for extracting data out of SAP Operational Data Provisioning data sources. Operational Data Provisioning can expose the full load and delta data using OData services. The connector is built off of the Kafka Connect framework, and therefore automatically supports pluggable converters such as Avro or JSON, single message transforms, exactly-once delivery, graceful back-off and other useful features. The connector uses the Java Connector SDK (JCo) library to connect to the SAP systems. It extracts delta data via ODP API function calls and emits the response into Kafka as a json payload. 

## Requirements


## Install Connect plugins

For distributed mode you must copy the connector and dependency jar files to all nodes in the cluster. Ensure plugin path is set in config/connect-distributed.properties instead of config/connect-standalone.properties. You control connectors in distributed mode using a REST API. Or download the executable JAR file and place it into one of the directories that is listed on the Connect worker's plugin.path configuration properties. This must be done on each of the installations where Connect will be run. See here for more detailed instructions. https://docs.confluent.io/home/connect/self-managed/userguide.html#connect-installing-plugins
To install a new plugin place it in the CLASSPATH of the Kafka Connect process. All the scripts for running Kafka Connect will use the CLASSPATH environment variable if it is set when they are invoked, making it easy to run with additional connector plugins:
In the same plugins directory you will also have to add the SAP jco jar(SAP Java Connector) which you can download from [here](https://support.sap.com/en/product/connectors/jco.html)

---

$ export CLASSPATH=/path/to/my/connectors/*

$ bin/connect-standalone standalone.properties new-custom-connector.properties

---

### Running Workers

#### Distributed

Start the worker process in distributed node:

bin/connect-distributed.sh config/connect-distributed.properties

Kafka Connect finds the plugins using a plugin path defined as a comma-separated list of directory paths in the plugin.path worker configuration property. The following shows an example plugin.path worker configuration property:

---
plugin.path=/usr/local/share/kafka/plugins

---

To install a plugin, place the plugin directory or uber JAR (or a symbolic link that resolves to one of these) in a directory already listed in the plugin path. You can update the plugin path by adding the absolute path of the directory containing the plugin. 
Using the plugin path example above, you would create a /usr/local/share/kafka/plugins directory on each machine running Connect and then place the plugin directories (or uber JARs) there.
When you start your Connect workers, each worker discovers all connectors, transforms, and converter plugins found inside the directories on the plugin path. 

## Connector Configs
Configuration Parameter | Description  
----------------------- | -----------
jco.client.ashost       | JCO Host
jco.client.sysnr        | This is the setting for SYSNY
jco.client.client       | This is the setting for SAP client
jco.client.user         | This is the setting for SAP user
jco.client.passwd       | This is the setting for SAP password
jco.client.lang         | This is the setting for language
jco.destination.pool_capacity | pool capacity
jco.destination.peak_limit    | peak limit
I_ODPNAME                     | ODP Name
I_CONTEXT                     | Context
I_SUBSCRIBER_TYPE             | Subscriber type
I_SUBSCRIBER_PROCESS          | Subscriber process
Topic                         | topic name
I_MAXPACKAGESIZE              | Max package size, request will be split up in multiple packages having this configured size
I_EXTRACTION_MODE             | D(Delta) or F(Full)


