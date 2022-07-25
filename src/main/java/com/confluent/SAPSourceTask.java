package com.confluent;

import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.Environment;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SAPSourceTask extends SourceTask {
  static final Logger log = LoggerFactory.getLogger(SAPSourceTask.class);

  private String odpName;
  private String odpContext;
  private String odpSubscriberType;
  private String odpSubscriberName;
  private String subscriberProcess;
  private String odpTOPIC;
  private BigDecimal ePointerFetch;
  private JCoDestination destination = null;
  private String destinationName ="ODPConnectorDestination";
  private JCoFunction function = null;
  private JCoFunction functionDetail = null;
  private JCoFunction functionFetch = null;
  private JCoFunction functionClose = null;
  //private final Schema VALUE_SCHEMA = Schema.BYTES_SCHEMA;
  private JCoTable fields;
  private String packageSize;
  SAPDestinationDataProvider sapDestinationDataProvider;
  private Schema odpSchema = null;
  private String extractionMode;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {

    log.info("entered start");
    AbstractConfig sapSourceConnectorConfig = new SAPSourceConnectorConfig(SAPSourceConnectorConfig.conf(), props);
    sapDestinationDataProvider = new SAPDestinationDataProvider();
    // building map for required properties
    Properties connectProperties = new Properties();

    String jcoHost = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_ASHOST);
    connectProperties.setProperty(sapDestinationDataProvider.JCO_ASHOST,jcoHost);

    String jcoSysnr = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_SYSNR);
    connectProperties.setProperty(sapDestinationDataProvider.JCO_SYSNR,jcoSysnr);

    String jcoClient = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_CLIENT);
    connectProperties.setProperty(sapDestinationDataProvider.JCO_CLIENT,jcoClient);

    String jcoUser = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_USER);
    connectProperties.setProperty(sapDestinationDataProvider.JCO_USER,jcoUser);

    String jcoPass = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_PASSWD);
    connectProperties.setProperty(sapDestinationDataProvider.JCO_PASSWD,jcoPass);

    String jcoLang = sapSourceConnectorConfig.getString(sapDestinationDataProvider.JCO_LANG);
    connectProperties.setProperty(sapDestinationDataProvider.JCO_LANG,jcoLang);

    String jcoPoolCapacity = String.valueOf(sapSourceConnectorConfig.getInt(sapDestinationDataProvider.JCO_POOL_CAPACITY));
    connectProperties.setProperty(sapDestinationDataProvider.JCO_POOL_CAPACITY,jcoPoolCapacity);

    String jcoPeakLimit = String.valueOf(sapSourceConnectorConfig.getInt(sapDestinationDataProvider.JCO_PEAK_LIMIT));
    connectProperties.setProperty(sapDestinationDataProvider.JCO_PEAK_LIMIT,jcoPeakLimit);



    // The destination name must match the name you use for looking up a destination.
    odpName = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.ODP_NAME);
    odpContext = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.ODP_CONTEXT);
    odpSubscriberType = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.SUBSCRIBER_TYPE);
    odpSubscriberName = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.SUBSCRIBER_NAME);
    subscriberProcess = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.SUBSCRIBER_PROCESS);
    odpTOPIC = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.ODPTopicName);
    packageSize = String.valueOf(sapSourceConnectorConfig.getInt(SAPSourceConnectorConfig.MAX_PACKAGE_SIZE));
    extractionMode = sapSourceConnectorConfig.getString(SAPSourceConnectorConfig.EXTRACTION_MODE);


    if(!Environment.isDestinationDataProviderRegistered()) {
      sapDestinationDataProvider.addDestination(destinationName, connectProperties);
      com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(sapDestinationDataProvider);
    }

    try {
      destination = JCoDestinationManager.getDestination(destinationName);
      function = destination.getRepository().getFunction("RODPS_REPL_ODP_OPEN");
      functionDetail = destination.getRepository().getFunction("RODPS_REPL_ODP_GET_DETAIL");
      //functionFetch = destination.getRepository().getFunction("RODPS_REPL_ODP_FETCH");
    } catch (JCoException e) {
      e.printStackTrace();
    }


    /*** function execute and input params for RODPS_REPL_ODP_OPEN */

    if (function==null)
      throw new RuntimeException("function not found in SAP.");
    JCoParameterList inputParamList = function.getImportParameterList();
    inputParamList.setValue(SAPSourceConnectorConfig.ODP_NAME, odpName);
    inputParamList.setValue(SAPSourceConnectorConfig.ODP_CONTEXT, odpContext);
    inputParamList.setValue(SAPSourceConnectorConfig.SUBSCRIBER_TYPE, odpSubscriberType);
    inputParamList.setValue(SAPSourceConnectorConfig.SUBSCRIBER_NAME, odpSubscriberName);
    inputParamList.setValue(SAPSourceConnectorConfig.EXTRACTION_MODE, extractionMode);
    //inputParamList.setValue("I_EXTRACTION_MODE", "F");
    inputParamList.setValue(SAPSourceConnectorConfig.SUBSCRIBER_PROCESS, subscriberProcess);

    JCoParameterList outputParamList = function.getExportParameterList();

    try
    {
      function.execute(destination);
      log.info("table data" + "...." + function.getTableParameterList().getTable("ET_RETURN").toString());
    }
    catch (AbapException e)
    {
      System.out.println(e);
      return;
    } catch (JCoException e) {
      e.printStackTrace();
    }

    ePointerFetch = outputParamList.getBigDecimal("E_POINTER");

    /*** function execute and input params for RODPS_REPL_ODP_GET_DETAIL */

    if (functionDetail==null)
      throw new RuntimeException("RFC_SYSTEM_INFO not found in SAP.");
    JCoParameterList inputParamListfordata = functionDetail.getImportParameterList();
    inputParamListfordata.setValue(SAPSourceConnectorConfig.ODP_NAME, odpName);
    inputParamListfordata.setValue(SAPSourceConnectorConfig.ODP_CONTEXT, odpContext);
    inputParamListfordata.setValue(SAPSourceConnectorConfig.SUBSCRIBER_TYPE, odpSubscriberType);

    try
    {
      functionDetail.execute(destination);
      fields = functionDetail.getTableParameterList().getTable("ET_FIELDS");
      SchemaBuilder odpSchemaBuilder = SchemaBuilder.struct().name("com.confluent.ODPTable."+odpTOPIC).version(1).doc("schema for odp "+odpTOPIC);

      for (int i=0; i<fields.getNumRows(); i++) {
        fields.setRow(i);
        String fieldName = fields.getString("NAME");
        String fieldType = fields.getString("TYPE");
        odpSchemaBuilder.field(fieldName,fieldType.equals("DEC")?Schema.FLOAT64_SCHEMA:Schema.STRING_SCHEMA);

      }
      odpSchema = odpSchemaBuilder.build();
    }
    catch (AbapException e)
    {
      System.out.println(e);
      return;
    } catch (JCoException e) {
      e.printStackTrace();
    }
  }

  private  List<SourceRecord> getJson(JCoTable field, JCoTable entireDate){

    List<SourceRecord> records = new ArrayList<>();
    List<Struct>  ret = new ArrayList<>();

    for (int j=0; j<entireDate.getNumRows(); j++) {
      Struct odpStruct = new Struct(odpSchema);
      entireDate.setRow(j);
      String data = new String(entireDate.getByteArray("DATA"), StandardCharsets.UTF_8);
      int currentPos=0;
      for (int i=0; i<field.getNumRows(); i++)
      {
        field.setRow(i);
        String fieldName = field.getString("NAME");
        int fieldOutput = field.getInt("OUTPUTLENG");
        String fieldType = field.getString("TYPE");
        if(fieldType.equals("DEC"))
          odpStruct.put(fieldName,Double.parseDouble(data.substring(currentPos, currentPos+fieldOutput).trim()));
        else
          odpStruct.put(fieldName, data.substring(currentPos, currentPos+fieldOutput).trim());

        currentPos=currentPos+fieldOutput;
      }
      ret.add(odpStruct);
      records.add(new SourceRecord(null, null, odpTOPIC, null,
              null, null, odpSchema, odpStruct, System.currentTimeMillis()));
    }
    return records;
  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
    /*** function execute and input params for RODPS_REPL_ODP_FETCH */
    JCoDestination destination = null;
    JCoFunction functionFetch = null;

    try {
      destination = JCoDestinationManager.getDestination(destinationName);
    } catch (JCoException e) {
      e.printStackTrace();
    }
    try {
      functionFetch = destination.getRepository().getFunction("RODPS_REPL_ODP_FETCH");
    } catch (JCoException e) {
      e.printStackTrace();
    }
    if (functionFetch==null)
      throw new RuntimeException("RFC_SYSTEM_INFO not found in SAP.");
    JCoParameterList inputParamListforfetch = functionFetch.getImportParameterList();
    inputParamListforfetch.setValue("I_POINTER", ePointerFetch);
    inputParamListforfetch.setValue(SAPSourceConnectorConfig.MAX_PACKAGE_SIZE, packageSize);

    try {
      functionFetch.execute(destination);
    } catch (JCoException e) {
      e.printStackTrace();
    }

    List<SourceRecord> returnRecords = getJson(fields, functionFetch.getTableParameterList().getTable("ET_DATA"));
    return returnRecords;
  }

  @Override
  public void stop() {
    /*** function execute and input params for RODPS_REPL_ODP_CLOSE */

    log.trace("Stopping");
    JCoDestination destination = null;
    JCoFunction functionClose = null;

    try {
      destination = JCoDestinationManager.getDestination(destinationName);

      functionClose = destination.getRepository().getFunction("RODPS_REPL_ODP_CLOSE");

    } catch (JCoException e) {
      e.printStackTrace();
    }

    if (functionClose==null)
      throw new RuntimeException("RODPS_REPL_ODP_CLOSE not found in SAP.");
    JCoParameterList inputParamListforClose = functionClose.getImportParameterList();
    inputParamListforClose.setValue("I_POINTER", ePointerFetch);

    try {
      functionClose.execute(destination);
      //System.out.println(functionClose.getTableParameterList().getTable("ET_RETURN"));
    } catch (JCoException e) {
      e.printStackTrace();
      return;
    }

  }
}