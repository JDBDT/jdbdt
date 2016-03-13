package org.jdbdt.tcfg.mariadb;

import org.jdbdt.DBCfg;
import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class MariaDBSuite extends DBEngineTestSuite {
  @BeforeClass 
  public static void setup() throws ClassNotFoundException, InstantiationException, IllegalAccessException { 
    DBCfg.getConfig().setDriver("org.mariadb.jdbc.Driver");
    DBCfg.getConfig().setURL("jdbc:mariadb://localhost:3316");
  }
}
