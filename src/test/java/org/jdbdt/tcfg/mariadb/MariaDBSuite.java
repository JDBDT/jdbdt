package org.jdbdt.tcfg.mariadb;

import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class MariaDBSuite extends DBEngineTestSuite {
  @BeforeClass 
  public static void setup() throws ClassNotFoundException, InstantiationException, IllegalAccessException { 
    Class.forName("org.mariadb.jdbc.Driver");
    System.setProperty(DB_URL_PROP, "jdbc:mariadb://localhost:3306");
  }
}
