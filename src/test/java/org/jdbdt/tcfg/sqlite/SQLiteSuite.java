package org.jdbdt.tcfg.sqlite;

import org.jdbdt.DBEngineTestSuite;
import org.jdbdt.JDBDT;
import org.jdbdt.StatementPoolingEnabled;
import org.jdbdt.TruncateSupportEnabled;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.ExcludeCategory;

@SuppressWarnings("javadoc")
@RunWith(Categories.class)
@ExcludeCategory({
  TruncateSupportEnabled.class,
  StatementPoolingEnabled.class
})
public class SQLiteSuite extends DBEngineTestSuite {
  @BeforeClass 
  public static void setup() throws ClassNotFoundException, InstantiationException, IllegalAccessException { 
    Class.forName("org.sqlite.JDBC");
    System.setProperty(DB_URL_PROP, "jdbc:sqlite:jdbdt-sqlite-test.db");
    System.setProperty(DB_DATE_UNSUPPORTED_PROP, "true");
    JDBDT.disableStatementPooling();
  }
}
