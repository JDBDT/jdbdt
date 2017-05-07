package org.jdbdt.tcfg.sqlite;

import org.jdbdt.DBConfig;
import org.jdbdt.DBEngineTestSuite;
import org.jdbdt.StatementReuseEnabled;
import org.jdbdt.TruncateSupportEnabled;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.ExcludeCategory;

@SuppressWarnings("javadoc")
@RunWith(Categories.class)
@ExcludeCategory({
  TruncateSupportEnabled.class,
  StatementReuseEnabled.class
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
