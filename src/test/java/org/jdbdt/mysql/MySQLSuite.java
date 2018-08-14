package org.jdbdt.mysql;

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
public class MySQLSuite extends DBEngineTestSuite {

  @BeforeClass 
  public static void setup() throws ClassNotFoundException {
    DBConfig.getConfig()
      .reset()
      .setDriver("com.mysql.jdbc.Driver")
      .setURL(MySQLDBSetup.get().start());
  }
  
  @AfterClass
  public static void teardown() {
    MySQLDBSetup.get().stop();
  }
 
}
