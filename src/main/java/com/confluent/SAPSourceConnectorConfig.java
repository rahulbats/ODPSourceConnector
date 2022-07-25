package com.confluent;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class SAPSourceConnectorConfig extends AbstractConfig {

  // documentation variables
  private static final String MY_SETTING_JCO_HOST_DOC = "This is the setting for SAP Host";
  private static final String MY_SETTING_JCO_SYSNR_DOC = "This is the setting for SYSNY";
  private static final String MY_SETTING_JCO_CLIENT_DOC = "This is the setting for SAP client";
  private static final String MY_SETTING_JCO_USER_DOC = "This is the setting for SAP user";
  private static final String MY_SETTING_JCO_PASSWD_DOC = "This is the setting for SAP password";
  private static final String MY_SETTING_JCO_LANG_DOC = "This is the setting for language";
  private static final String MY_SETTING_JCO_POOL_CAPACITY_DOC = "";
  private static final String MY_SETTING_JCO_PEAK_LIMIT_DOC = "";
  private static final String MY_SETTING_ODP_NAME_DOC = "his is the setting for name for each ODP";
  private static final String MY_SETTING_ODP_CONTEXT_DOC = "his is the setting for context for each ODP";
  private static final String MY_SETTING_SUBSCRIBER_TYPE_DOC = "";
  private static final String MY_SUBSCRIBER_NAME_DOC = "";
  private static final String MY_SETTING_SUBSCRIBER_PROCESS_DOC = "";
  private static final String MY_SETTING_ODPSAPITEST_DOC ="this is the setting for kafka topic name the data for this ODP will be pushed to";
  private static final String MY_SETTING_MAX_PACKAGE_SIZE_DOC= "A delta request will be split up in multiple packages having this configured size";
  private static final String MY_SETTING_EXTRACTION_MODE_DOC = "Choose the type of data extraction: full or delta";

  // additional config other than JCO
  public static final String ODP_NAME="I_ODPNAME";
  public static final String ODP_CONTEXT = "I_CONTEXT";
  public static final String SUBSCRIBER_TYPE ="I_SUBSCRIBER_TYPE";
  public static final String SUBSCRIBER_NAME = "I_SUBSCRIBER_NAME";
  public static final String SUBSCRIBER_PROCESS = "I_SUBSCRIBER_PROCESS";
  public static final String ODPTopicName = "Topic";
  public static final String MAX_PACKAGE_SIZE = "I_MAXPACKAGESIZE";
  public static final String EXTRACTION_MODE = "I_EXTRACTION_MODE";

  public SAPSourceConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
    super(config, parsedConfig);
  }

  public static ConfigDef conf() {
    ConfigDef.Recommender recommender = new ConfigDef.Recommender() {
      @Override
      public List<Object> validValues(String s, Map<String, Object> map) {
        return Arrays.asList((Object)"D", (Object)"F", (Object)"R");
      }

      @Override
      public boolean visible(String s, Map<String, Object> map) {
        return true;
      }
    };

    SAPDestinationDataProvider sapDestinationDataProvider = new SAPDestinationDataProvider();
    return new ConfigDef()
            .define(sapDestinationDataProvider.JCO_ASHOST, Type.STRING, Importance.HIGH, MY_SETTING_JCO_HOST_DOC)
            .define(sapDestinationDataProvider.JCO_SYSNR, Type.STRING, Importance.HIGH, MY_SETTING_JCO_SYSNR_DOC)
            .define(sapDestinationDataProvider.JCO_CLIENT, Type.STRING, Importance.HIGH, MY_SETTING_JCO_CLIENT_DOC)
            .define(sapDestinationDataProvider.JCO_USER, Type.STRING, Importance.HIGH, MY_SETTING_JCO_USER_DOC)
            .define(sapDestinationDataProvider.JCO_PASSWD, Type.STRING, Importance.HIGH, MY_SETTING_JCO_PASSWD_DOC)
            .define(sapDestinationDataProvider.JCO_LANG, Type.STRING, Importance.HIGH, MY_SETTING_JCO_LANG_DOC)
            .define(sapDestinationDataProvider.JCO_POOL_CAPACITY, Type.INT, Importance.HIGH, MY_SETTING_JCO_POOL_CAPACITY_DOC)
            .define(sapDestinationDataProvider.JCO_PEAK_LIMIT, Type.INT, Importance.HIGH, MY_SETTING_JCO_PEAK_LIMIT_DOC)
            .define(ODP_NAME, Type.STRING, Importance.HIGH, MY_SETTING_ODP_NAME_DOC)
            .define(ODP_CONTEXT, Type.STRING, Importance.HIGH, MY_SETTING_ODP_CONTEXT_DOC)
            .define(SUBSCRIBER_TYPE, Type.STRING, Importance.HIGH, MY_SETTING_SUBSCRIBER_TYPE_DOC)
            .define(SUBSCRIBER_NAME, Type.STRING, Importance.HIGH, MY_SUBSCRIBER_NAME_DOC)
            .define(SUBSCRIBER_PROCESS, Type.STRING, Importance.HIGH, MY_SETTING_SUBSCRIBER_PROCESS_DOC)
            .define(ODPTopicName, Type.STRING, Importance.HIGH, MY_SETTING_ODPSAPITEST_DOC)
            .define(MAX_PACKAGE_SIZE, Type.INT, Importance.HIGH, MY_SETTING_MAX_PACKAGE_SIZE_DOC)
            .define(EXTRACTION_MODE, Type.STRING, "D",(ConfigDef.Validator)null,Importance.HIGH, MY_SETTING_EXTRACTION_MODE_DOC,(String)null, -1, ConfigDef.Width.NONE,EXTRACTION_MODE, recommender);
  }
}