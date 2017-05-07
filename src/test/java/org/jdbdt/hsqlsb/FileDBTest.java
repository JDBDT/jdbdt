package org.jdbdt.hsqlsb;

import org.jdbdt.DBConfig;
import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class FileDBTest extends DBEngineTestSuite {

  @BeforeClass 
  public static void setup() { 
    DBConfig.getConfig().setURL(HSQLDBSuite.FILE_DB_URL);
  }
}
