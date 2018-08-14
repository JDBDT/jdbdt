package org.jdbdt.postgresql;

import org.jdbdt.DBConfig;
import org.jdbdt.DBEngineTestSuite;
import org.jdbdt.TestCategories;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.ExcludeCategory;
import org.junit.runner.RunWith;

@SuppressWarnings("javadoc")
@RunWith(Categories.class)
@ExcludeCategory({
  TestCategories.CaseSensitive.class,
})
public class PostgreSQLSuite extends DBEngineTestSuite {

  @BeforeClass 
  public static void setup() { 
    DBConfig.getConfig()
    .reset()
    .setDriver("org.postgresql.Driver")
    .setURL(PostgreSQLSetup.get().start());
  }

  @AfterClass
  public static void teardown() {
    PostgreSQLSetup.get().stop();
  }
}
