package org.jdbdt.tcfg.derby;

import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class FileDBTest extends DBEngineTestSuite {
  @BeforeClass 
  public static void setup() { 
    System.setProperty(DB_URL_PROP, "jdbc:derby:jdbdt-derby-test;create=true");
  }
}
