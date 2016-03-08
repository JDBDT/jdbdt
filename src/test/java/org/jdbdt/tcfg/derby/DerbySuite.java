package org.jdbdt.tcfg.derby;

import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;


@SuppressWarnings("javadoc")
public class DerbySuite extends DBEngineTestSuite {
  @BeforeClass 
  public static void setup() throws ClassNotFoundException { 
    Class.forName("org.h2.Driver");
    System.setProperty(DB_URL_PROP, "jdbc:derby:jdbdt-derby-test;create=true");
  }
}
