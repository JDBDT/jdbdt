package org.jdbdt;

import org.junit.BeforeClass;

@SuppressWarnings("javadoc")
public class HSQLDBMemSuite extends DBEngineTestSuite {

 @BeforeClass 
 public static void setup() { 
   try {
     Class.forName("org.hsqldb.jdbcDriver");
     System.setProperty(DB_URL_PROP, "jdbc:hsqldb:mem:jdbdt-test;shutdown=true");
   } catch(Throwable e) {
     throw new Error(e);
   } 
 }
}
