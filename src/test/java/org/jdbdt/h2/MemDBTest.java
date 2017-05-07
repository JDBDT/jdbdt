package org.jdbdt.h2;

import org.jdbdt.DBConfig;
import org.jdbdt.DBEngineTestSuite;
import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class MemDBTest extends DBEngineTestSuite {
  @BeforeClass 
  public static void setup() { 
    DBConfig.getConfig().setURL("jdbc:h2:mem:jdbdt-h2-test");
  }
}
