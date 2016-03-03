package org.jdbdt;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({
  StatementPoolTest.class,
  TableTest.class,
  LoaderTest.class,
  TypedLoaderTest.class,
  TearDownTest.class,
  ObserverTest.class,
  TypedObserverTest.class,
  DataSetTest.class,
  DataSetFillersTest.class,
  DataSetSequenceFillersTest.class,
  DataSetRandomFillersTest.class,
  IntegrationTest.class
})
public class AllTests {

}
