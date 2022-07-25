# ODPSourceConnector
## Overview
This SAP ODP connector is a Kafka Connector for extracting data out of SAP Operational Data Provisioning data sources. Operational Data Provisioning can expose the full load and delta data using OData services. The connector is built off of the Kafka Connect framework, and therefore automatically supports pluggable converters such as Avro or JSON, single message transforms, exactly-once delivery, graceful back-off and other useful features. The connector uses the Java Connector SDK (JCo) library to connect to the SAP systems. It extracts delta data via ODP API function calls and emits the response into Kafka as a json payload. 

## Requirements

## Classes 

1. The SAPSourceConnector class

This class inherits from SourceConnector class which in turn inherits from connecor class form the Kakfa library. We add all the fields here that will store parsed configuration information. TaskClass() method defines the class that should be instantiated in worker processes to actually read the data. TaskConfigs() method: which returns a list of maps containing the configuration properties each task will use to stream data into or out of Kafka. The method accepts an int value for the maximum number of tasks to run in parallel and is pulled from the tasks.max configuration property that is provided on startup.



2. The SAPSourceTask class
As with the Connector class, Task includes abstract methods for start, stop, and version. Most of the logic for streaming data into Kafka, however, will occur in the poll method, which is continually called by the Kafka Connect framework for each task. 
First, the start() method calls the RODPS_REPL_ODP_OPEN and RODPS_REPL_ODP_GET_DETAIL function modules from SAP ODP. They use the following input parameteres to establish the connection: ODP_NAME, ODP_CONTEXT, SUBSCRIBER_TYPE, SUBSCRIBER_NAME, EXTRACTION_MODE,  SUBSCRIBER_PROCESS and exports an output parameter E_POINTER. 
The getJson() method iterates through the columns in the SAP table and the return data to convert them into a json key-value pairs of type 'List<SourceRecord>'. It uses this information to create an output SourceRecord with four pieces of information: the source partition (there is only one, the single file being read), source offset (position in the file), output topic name, and output value (the line, including a schema indicating this value will always be a string). Other variants of the SourceRecord constructor can also include a specific output partition and a key. Source partitions and source offsets are simply a Map that can be used to keep track of the source data that has already been copied to Kafka. With each iteration, the method also defines a struct schema according to the data type of each field in the SAP table. The poll() method makes a call to the RODPS_REPL_ODP_FETCH function module along with it's input paramters: I_POINTER (which uses the ePointerFetch from RODPS_REPL_ODP_OPEN function) and MAX_PACKAGE_SIZE( to specify the size of data in bytes). 
This poll() method is going to be called repeatedly, and for each call it will call the getJson()method and will try to read records from each fetch call. Finally, the stop() method makes a call to the RODPS_REPL_ODP_CLOSE function module to close the connection with SAP. 

3. The SAPSourceConnectorConfig
this class is an implementation of the config() to expose the configuration definition to the framework. The following code in SAPSourceConnector defines the configuration and exposes it to the framework. The ConfigDef class is used for specifying the set of expected configurations. For each configuration, you can specify the name, the type, the default value, the documentation, the group information, the order in the group, the width of the configuration value and the name suitable for display in the UI.


## Install Connect plugins

For distributed mode you must copy the connector and dependency jar files to all nodes in the cluster. Ensure plugin path is set in config/connect-distributed.properties instead of config/connect-standalone.properties. You control connectors in distributed mode using a REST API. Or download the executable JAR file and place it into one of the directories that is listed on the Connect worker's plugin.path configuration properties. This must be done on each of the installations where Connect will be run. See here for more detailed instructions. https://docs.confluent.io/home/connect/self-managed/userguide.html#connect-installing-plugins
To install a new plugin place it in the CLASSPATH of the Kafka Connect process. All the scripts for running Kafka Connect will use the CLASSPATH environment variable if it is set when they are invoked, making it easy to run with additional connector plugins:

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
In the same plugins directory you will also have to add the SAP jco jar which you can download from [here](https://support.sap.com/en/product/connectors/jco.html) 

## Connector Configs

### Converters
The key.converter and value.converter properties are where you specify the type of converter to use.

JsonConverter org.apache.kafka.connect.json.JsonConverter (without Schema Registry): use with structured data

StringConverter org.apache.kafka.connect.storage.StringConverter: simple string format

To use the AvroConverter with Schema Registry, you specify the key.converter and value.converter properties in the worker configuration. An additional converter property must also be added that provides the Schema Registry URL. 
