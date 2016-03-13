package org.jdbdt.tcfg.h2;

import org.jdbdt.DBCfg;
import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class FileDBTest extends DBEngineTestSuite {
  @BeforeClass 
  public static void setup() { 
    DBCfg.getConfig().setURL("jdbc:h2:./jdbdt-h2-test");
  }
}
