package org.jdbdt.derby;

import org.jdbdt.DBConfig;
import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;


@SuppressWarnings("javadoc")
public class DerbySuite extends DBEngineTestSuite {
  @BeforeClass 
  public static void setup() throws ClassNotFoundException { 
    DBConfig.getConfig()
      .reset()
      .setDriver("org.apache.derby.jdbc.EmbeddedDriver")
      .setURL("jdbc:derby:jdbdt-derby-test;create=true")
      .countReturnsInteger();
  }
}
