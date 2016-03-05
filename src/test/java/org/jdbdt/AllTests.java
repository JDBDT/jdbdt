package org.jdbdt;

import org.jdbdt.tcfg.h2.H2Suite;
import org.jdbdt.tcfg.hsqlsb.HSQLDBSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({
  HSQLDBSuite.class,
  H2Suite.class,
})
public class AllTests {
  
}
