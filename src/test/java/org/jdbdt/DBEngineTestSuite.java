package org.jdbdt;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({
  StatementPoolTest.class,
  TableTest.class,
  TearDownTest.class,
  DataInsertionTest.class,
  DeltaTest.class,
  DataBuilderCoreTest.class,
  DataBuilderCoreFillerTest.class,
  DataBuilderSequenceFillersTest.class,
  DataBuilderRandomFillersTest.class,
//  IntegrationTest.class
})
public class DBEngineTestSuite {
}
