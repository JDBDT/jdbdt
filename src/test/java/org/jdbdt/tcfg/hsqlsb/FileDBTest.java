package org.jdbdt.tcfg.hsqlsb;

import org.jdbdt.DBCfg;
import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class FileDBTest extends DBEngineTestSuite {

  @BeforeClass 
  public static void setup() { 
    DBCfg.getConfig().setURL(HSQLDBSuite.FILE_DB_URL);
  }
}
