package com.confluent;

import java.util.*;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SAPSourceConnector extends SourceConnector {
  private static Logger log = LoggerFactory.getLogger(SAPSourceConnector.class);
  private SAPSourceConnectorConfig config;

  private String jcoHost;
  private String jcoSysnr;
  private String jcoClient;
  private String jcoUser;
  private String jcoPass;
  private String jcoLang;
  private String jcoPoolCapacity;
  private String jcoPeakLimit;
  private String odpName;
  private String odpContext;
  private String odpSubscriberType;
  private String odpSubscriberName;
  private String subscriberProcess;
  private String odptopic;
  private String packageSize;
  private String extractionMode;


  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {

    AbstractConfig sapSourceConnectorConfig = new SAPSourceConnectorConfig(config(), props);
    SAPDestinationDataProvider sapDestinationDataProvider = new SAPDestinationDataProvider();

    jcoHost = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_ASHOST);
    jcoSysnr = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_SYSNR);
    jcoClient = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_CLIENT);
    jcoUser = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_USER);
    jcoPass = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_PASSWD);
    jcoLang = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_LANG);
    jcoPoolCapacity = String.valueOf(sapSourceConnectorConfig.getInt(sapDestinationDataProvider.JCO_POOL_CAPACITY));
    jcoPeakLimit = String.valueOf(sapSourceConnectorConfig.getInt(sapDestinationDataProvider.JCO_PEAK_LIMIT));
    odpName = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.ODP_NAME);
    odpContext = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.ODP_CONTEXT);
    odpSubscriberType = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.SUBSCRIBER_TYPE);
    odpSubscriberName = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.SUBSCRIBER_NAME);
    subscriberProcess = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.SUBSCRIBER_PROCESS);
    odptopic = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.ODPTopicName);
    packageSize =  String.valueOf(sapSourceConnectorConfig.getInt(SAPSourceConnectorConfig.MAX_PACKAGE_SIZE));
    extractionMode = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.EXTRACTION_MODE);
  }

  @Override
  public Class<? extends Task> taskClass() {
    return SAPSourceTask.class;
  }

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {
    if (maxTasks!=1) {
      log.info("Ignoring maxtasks as there can only be 1");
    }
    SAPDestinationDataProvider sapDestinationDataProvider = new SAPDestinationDataProvider();
    List<Map<String, String>> configs = new ArrayList<>(maxTasks);
    Map<String, String> config = new HashMap<>();

    config.put(sapDestinationDataProvider.JCO_ASHOST,jcoHost);
    config.put(sapDestinationDataProvider.JCO_SYSNR,jcoSysnr);
    config.put(sapDestinationDataProvider.JCO_CLIENT,jcoClient);
    config.put(sapDestinationDataProvider.JCO_USER,jcoUser);
    config.put(sapDestinationDataProvider.JCO_PASSWD,jcoPass);
    config.put(sapDestinationDataProvider.JCO_LANG,jcoLang);
    config.put(sapDestinationDataProvider.JCO_POOL_CAPACITY,jcoPoolCapacity);
    config.put(sapDestinationDataProvider.JCO_PEAK_LIMIT,jcoPeakLimit);
    config.put(SAPSourceConnectorConfig.ODP_NAME,odpName);
    config.put(SAPSourceConnectorConfig.ODP_CONTEXT,odpContext);
    config.put(SAPSourceConnectorConfig.SUBSCRIBER_TYPE,odpSubscriberType);
    config.put(SAPSourceConnectorConfig.SUBSCRIBER_NAME,odpSubscriberName);
    config.put(SAPSourceConnectorConfig.SUBSCRIBER_PROCESS,subscriberProcess);
    config.put(SAPSourceConnectorConfig.ODPTopicName, odptopic);
    config.put(SAPSourceConnectorConfig.MAX_PACKAGE_SIZE, packageSize);
    config.put(SAPSourceConnectorConfig.EXTRACTION_MODE, extractionMode);
    configs.add(config);
    return configs;

  }

  @Override
  public void stop() {
    // not required
  }

  @Override
  public ConfigDef config() {
    return SAPSourceConnectorConfig.conf();
  }
}