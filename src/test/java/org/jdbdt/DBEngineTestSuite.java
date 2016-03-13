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
  DeltaTest.class,
  TypedDeltaTest.class,
  DataSetTest.class,
  DataSetFillersTest.class,
  DataSetSequenceFillersTest.class,
  DataSetRandomFillersTest.class,
//  IntegrationTest.class
})
public class DBEngineTestSuite {
  public static final String DB_DATE_UNSUPPORTED_PROP = "org.jdbdt.test.DATA_UNSUPPORTED";
}
