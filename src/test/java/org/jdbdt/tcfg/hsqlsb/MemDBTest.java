package org.jdbdt.tcfg.hsqlsb;

import org.jdbdt.DBCfg;
import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class MemDBTest extends DBEngineTestSuite {
  @BeforeClass 
  public static void setup() { 
    DBCfg.getConfig().setURL(HSQLDBSuite.MEM_DB_URL);
  }
}
