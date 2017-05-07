package org.jdbdt.sqlite;

import org.jdbdt.DBConfig;
import org.jdbdt.DBEngineTestSuite;
import org.jdbdt.TestCategories;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.ExcludeCategory;

@SuppressWarnings("javadoc")
@RunWith(Categories.class)
@ExcludeCategory({
  TestCategories.Truncate.class,
  TestCategories.StatementReuse.class
})
public class SQLiteSuite extends DBEngineTestSuite {
  @BeforeClass 
  public static void setup() throws ClassNotFoundException { 
    DBConfig.getConfig()
      .reset()
      .setDriver("org.sqlite.JDBC")
      .setURL("jdbc:sqlite:jdbdt-sqlite-test.db")
      .dateNotSupported()
      .countReturnsInteger()
      .doNotReuseStatements();
  }
}
