package org.jdbdt;

import static org.junit.Assert.*;
import static org.jdbdt.JDBDT.*;
import static org.jdbdt.StatementPool.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("javadoc")
@Category(StatementPoolingEnabled.class)
public class StatementPoolTest extends DBTestCase {

  private static Table table;
  
  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = 
        table(UserDAO.TABLE_NAME)
          .columns(UserDAO.COLUMNS)
          .boundTo(getConnection());
  }
  
  @Before 
  public void setup() {
    StatementPool.enable();
  }
  
  @Test
  public void test01() throws SQLException {
    PreparedStatement s1 = compile(getConnection(), "SELECT * FROM " + UserDAO.TABLE_NAME);
    PreparedStatement s2 = compile(getConnection(), "SELECT * FROM " + UserDAO.TABLE_NAME);
    assertSame(s1, s2);
  }
  
  @Test
  public void test02() throws SQLException {
    Query q = selectFrom(table);
    PreparedStatement s1 = q.getQueryStatement();
    PreparedStatement s2 = compile(getConnection(), q.getSQLForQuery());
    assertSame(s1, s2);
  }
  
  @Test
  public void test03() throws SQLException {
    Query q1 = selectFrom(table);
    Query q2 = selectFrom(table).where("login='foo'");
    Query q3 = selectFrom(table);
    PreparedStatement s1 = q1.getQueryStatement();
    PreparedStatement s2 = q2.getQueryStatement();
    PreparedStatement s3 = q3.getQueryStatement();
    assertNotSame(s1, s2);
    assertSame(s1, s3);
  }
  
  @Test
  public void test04() throws SQLException {
    Query q1 = selectFrom(table).where("login='foo'");
    Query q2 = selectFrom(table).where("login='foo'");
    PreparedStatement s1 = q1.getQueryStatement();
    PreparedStatement s2 = q2.getQueryStatement();
    assertSame(s1, s2);
  }

  @Test
  public void test05() throws SQLException {
    StatementPool.disable();
    Query q1 = selectFrom(table);
    Query q2 = selectFrom(table).where("login='foo'");
    PreparedStatement s1 = q1.getQueryStatement();
    PreparedStatement s2 = q2.getQueryStatement();
    assertNotSame(s1, s2);
  }
}
