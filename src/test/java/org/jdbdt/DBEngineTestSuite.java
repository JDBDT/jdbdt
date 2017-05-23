package org.jdbdt;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({
  TableTest.class,
  QueryBuilderTest.class,
  QueryTest.class,
  DataSetTest.class,
  DBSetupTest.class,
  DBAssertTest.class,
  DBStatementReuseTest.class,
  DBSavepointTest.class,
  AssertEqualsTest.class,
  DataSetBuilderCoreTest.class,
  DataSetBuilderCoreFillerTest.class,
  IntegrationTest.class
})
public class DBEngineTestSuite {
}
