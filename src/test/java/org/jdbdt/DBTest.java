package org.jdbdt;

import static org.junit.Assert.*;
import static org.jdbdt.JDBDT.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("javadoc")
public class DBTest extends DBTestCase {

  private PreparedStatement compile(String sql) throws SQLException {
    return getDB().compile(sql);
  }

  @Test @Category(StatementReuseEnabled.class)
  public void testReuse1() throws SQLException {
    PreparedStatement s1 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    PreparedStatement s2 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    assertSame(s1, s2);
  }

  @Test @Category(StatementReuseEnabled.class)
  public void testReuse2() throws SQLException {
    getDB().disable(DB.Option.REUSE_STATEMENTS);
    PreparedStatement s1 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    PreparedStatement s2 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    getDB().enable(DB.Option.REUSE_STATEMENTS);
    assertNotSame(s1, s2);
  }

  private class RestoreAutoCommit implements AutoCloseable {
    boolean initially;
    Connection connection;
    RestoreAutoCommit(DB db, boolean enable) throws SQLException {
      connection = db.getConnection();
      initially = db.getConnection().getAutoCommit();
      db.getConnection().setAutoCommit(enable);
    }
    @Override
    public void close() throws SQLException {
      connection.setAutoCommit(initially);
    }
    
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testSaveAndRestore0() throws SQLException {
    try (RestoreAutoCommit r = new RestoreAutoCommit(getDB(), true)) {
      save(getDB());
    }
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testSaveAndRestore1() throws SQLException {
    restore(getDB());
  }
  
  @Test
  public void testSaveAndRestore2() throws SQLException {
    try (RestoreAutoCommit r = new RestoreAutoCommit(getDB(), false))
    {
      User u = getDAO().query(EXISTING_DATA_ID1);
      String name1 = u.getName();
      String name2 = "Mr. " + u.getName();
      save(getDB());
      u.setName(name2);
      getDAO().doUpdate(u);
      String qname2 = getDAO().query(EXISTING_DATA_ID1).getName();
      restore(getDB());
      String qname1 = getDAO().query(EXISTING_DATA_ID1).getName();
      assertEquals(name1, qname1);
      assertEquals(name2, qname2);
    }
  }
}
