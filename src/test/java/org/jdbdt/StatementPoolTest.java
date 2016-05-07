package org.jdbdt;

import static org.junit.Assert.*;


import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("javadoc")
@Category(StatementPoolingEnabled.class)
public class StatementPoolTest extends DBTestCase {
  
  private PreparedStatement compile(String sql) throws SQLException {
    return getDB().compile(sql);
  }
  @Before 
  public void setup() {
    getDB().enable(DB.Option.REUSE_STATEMENTS);
  }
  
  @Test
  public void test01() throws SQLException {
    PreparedStatement s1 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    PreparedStatement s2 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    assertSame(s1, s2);
  }
  
  @Test
  public void test02() throws SQLException {
    getDB().disable(DB.Option.REUSE_STATEMENTS);
    PreparedStatement s1 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    PreparedStatement s2 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    assertNotSame(s1, s2);
  }
  
}
