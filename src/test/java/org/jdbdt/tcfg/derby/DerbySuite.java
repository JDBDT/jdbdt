package org.jdbdt.tcfg.derby;

import org.jdbdt.DBCfg;
import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;


@SuppressWarnings("javadoc")
public class DerbySuite extends DBEngineTestSuite {
  @BeforeClass 
  public static void setup() throws ClassNotFoundException { 
    DBCfg.getConfig().setDriver("org.h2.Driver");
    DBCfg.getConfig().setURL("jdbc:derby:jdbdt-derby-test;create=true");
  }
}
