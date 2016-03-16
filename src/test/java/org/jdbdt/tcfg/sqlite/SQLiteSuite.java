package org.jdbdt.tcfg.sqlite;

import org.jdbdt.DBCfg;
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
  public static void setup() throws ClassNotFoundException { 
    DBCfg.getConfig().setDriver("org.sqlite.JDBC");
    DBCfg.getConfig().setURL("jdbc:sqlite:jdbdt-sqlite-test.db");
    DBCfg.getConfig().dateNotSupported();
    JDBDT.disableStatementPooling();
  }
}
