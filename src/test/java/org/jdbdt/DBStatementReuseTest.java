package org.jdbdt;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("javadoc")
public class DBStatementReuseTest extends DBTestCase {

  private PreparedStatement compile(String sql) throws SQLException {
    return getDB().compile(sql).getStatement();
  }

  @Test @Category(TestCategories.StatementReuse.class)
  public void testReuse1() throws SQLException {
    PreparedStatement s1 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    PreparedStatement s2 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    assertSame(s1, s2);
  }

  @Test 
  public void testReuse2() throws SQLException {
    getDB().disable(DB.Option.REUSE_STATEMENTS);
    PreparedStatement s1 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    PreparedStatement s2 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    getDB().enable(DB.Option.REUSE_STATEMENTS);
    assertNotSame(s1, s2);
  }
  
  @After
  public void afterEachTest() {
    getDB().enable(DB.Option.REUSE_STATEMENTS);
  }

  
}