package org.jdbdt.tcfg.hsqlsb;

import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class FileDBTest extends DBEngineTestSuite {

  @BeforeClass 
  public static void setup() { 
    System.setProperty(DB_URL_PROP, "jdbc:hsqldb:file:jdbdt-hsqldb-test;shutdown=true");
  }
}
