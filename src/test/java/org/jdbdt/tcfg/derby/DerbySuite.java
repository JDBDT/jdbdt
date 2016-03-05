package org.jdbdt.tcfg.derby;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({ FileDBTest.class })
public class DerbySuite {
  @BeforeClass 
  public static void setup() throws ClassNotFoundException { 
    Class.forName("org.h2.Driver");
  }
}
