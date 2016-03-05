package org.jdbdt.tcfg.h2;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({ MemDBTest.class, FileDBTest.class })
public class H2Suite {
  @BeforeClass 
  public static void setup() throws ClassNotFoundException { 
    Class.forName("org.h2.Driver");
  }
}
