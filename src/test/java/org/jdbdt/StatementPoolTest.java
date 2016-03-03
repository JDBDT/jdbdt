package org.jdbdt;

import static org.junit.Assert.*;
import static org.jdbdt.JDBDT.*;
import static org.jdbdt.StatementPool.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("javadoc")
public class StatementPoolTest extends DBTestCase {

  private static Table table;
  private static TypedTable<User> typedTable;
  
  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = 
        table(UserDAO.TABLE_NAME)
          .columns(UserDAO.COLUMNS)
          .boundTo(getConnection());
    typedTable = table(UserDAO.TABLE_NAME,User.CONVERSION).columns(UserDAO.COLUMNS).boundTo(getConnection());
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
    PreparedStatement s1 = q.getStatement();
    PreparedStatement s2 = compile(getConnection(), q.toString());
    assertSame(s1, s2);
  }
  
  @Test
  public void test03() throws SQLException {
    Query q = selectFrom(table);
    // Inverse order to test02
    PreparedStatement s1 = compile(getConnection(), q.toString());
    PreparedStatement s2 = q.getStatement();
    assertSame(s1, s2);
  }
  
  @Test
  public void test04() throws SQLException {
    Query q1 = selectFrom(table);
    Query q2 = selectFrom(table);
    PreparedStatement s1 = q1.getStatement();
    PreparedStatement s2 = q2.getStatement();
    assertSame(s1, s2);
  }
  
  @Test
  public void test05() throws SQLException {
    Query q1 = selectFrom(table).where("login='foo'");
    Query q2 = selectFrom(table).where("login='foo'");
    PreparedStatement s1 = q1.getStatement();
    PreparedStatement s2 = q2.getStatement();
    assertSame(s1, s2);
  }
  
  @Test
  public void test06() throws SQLException {
    Query q1 = selectFrom(table);
    Query q2 = selectFrom(typedTable);
    PreparedStatement s1 = q1.getStatement();
    PreparedStatement s2 = q2.getStatement();
    assertSame(s1, s2);
  }
  
  @Test
  public void test07() throws SQLException {
    Query q1 = selectFrom(table).where("login='foo'");
    Query q2 = selectFrom(typedTable).where("login='foo'");
    PreparedStatement s1 = q1.getStatement();
    PreparedStatement s2 = q2.getStatement();
    assertSame(s1, s2);
  }
  
  @Test
  public void test09() throws SQLException {
    PreparedStatement s1 = delete(table);
    PreparedStatement s2 = delete(typedTable);
    assertSame(s1, s2);
  }
  
  @Test
  public void test10() throws SQLException {
    PreparedStatement s1 = StatementPool.truncate(table);
    PreparedStatement s2 = StatementPool.truncate(typedTable);
    assertSame(s1, s2);
  }
  
  @Test
  public void test11() throws SQLException {
    PreparedStatement s1 = insert(table);
    PreparedStatement s2 = insert(typedTable);
    assertSame(s1, s2);
  }

}
