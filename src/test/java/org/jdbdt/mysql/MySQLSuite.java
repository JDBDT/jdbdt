package org.jdbdt.mysql;

import org.jdbdt.DBConfig;
import org.jdbdt.DBEngineTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class MySQLSuite extends DBEngineTestSuite {

  @BeforeClass 
  public static void setup() throws ClassNotFoundException {
    DBConfig.getConfig()
      .reset()
      .setDriver("com.mysql.jdbc.Driver")
      .setURL(MySQLDBSetup.get().start());
  }
  
  @AfterClass
  public static void teardown() {
    MySQLDBSetup.get().stop();
  }
 
}
