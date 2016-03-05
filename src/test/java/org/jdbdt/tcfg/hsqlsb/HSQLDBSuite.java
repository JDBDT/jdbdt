package org.jdbdt.tcfg.hsqlsb;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({ FileDBTest.class, MemDBTest.class })
public class HSQLDBSuite {
  @BeforeClass 
  public static void setup() throws ClassNotFoundException { 
    Class.forName("org.hsqldb.jdbcDriver");
  }
}
