package org.jdbdt;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({
  AllNonDBTests.class,
  AllDBTests.class
})
public class AllTests {
  
}
