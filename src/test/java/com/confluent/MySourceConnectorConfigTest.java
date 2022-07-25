package com.confluent;

import org.junit.Test;

public class MySourceConnectorConfigTest {
  @Test
  public void doc() {
    System.out.println(SAPSourceConnectorConfig.conf().toRst());
  }
}