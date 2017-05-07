package org.jdbdt.hsqlsb;

import org.jdbdt.DBConfig;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({ FileDBTest.class, MemDBTest.class })
public class HSQLDBSuite {
  public static final String DRIVER = "org.hsqldb.jdbcDriver";
  public static final String MEM_DB_URL = "jdbc:hsqldb:mem:jdbdt-hsqldb-test;shutdown=true";;
  public static final String FILE_DB_URL = "jdbc:hsqldb:file:jdbdt-hsqldb-test;shutdown=true";
  
  @BeforeClass 
  public static void setup() throws ClassNotFoundException { 
    DBConfig.getConfig().reset().setDriver(DRIVER);
  }
}
