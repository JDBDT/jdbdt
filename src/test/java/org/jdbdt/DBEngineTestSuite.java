package org.jdbdt;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({
  StatementPoolTest.class,
  TableTest.class,
  QueryTest.class,
  SQLDataSourceTest.class,
  DataSetTest.class,
  DBSetupTest.class,
  DeltaTest.class,
  DataSetBuilderCoreTest.class,
  DataSetBuilderCoreFillerTest.class,
  DataSetBuilderSequenceFillersTest.class,
  DataSetBuilderRandomFillersTest.class,
//  IntegrationTest.class
})
public class DBEngineTestSuite {
}
