package org.jdbdt.postgresql;

import org.jdbdt.DBConfig;
import org.jdbdt.DBEngineTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class PostgreSQLSuite extends DBEngineTestSuite {

  @BeforeClass 
  public static void setup() { 
    DBConfig.getConfig()
    .reset()
    .setDriver("org.postgresql.Driver")
    .setURL(PostGresSQLSetup.get().start());
  }

  @AfterClass
  public static void teardown() {
    PostGresSQLSetup.get().stop();
  }
}
