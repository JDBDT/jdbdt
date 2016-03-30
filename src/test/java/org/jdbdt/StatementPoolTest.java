package org.jdbdt;

import static org.junit.Assert.*;

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
    table = getDB().table(UserDAO.TABLE_NAME)
                   .columns(UserDAO.COLUMNS);
  }
  
  private PreparedStatement compile(String sql) throws SQLException {
    return getDB().compile(sql);
  }
  @Before 
  public void setup() {
    getDB().enableStatementPooling();
  }
  
  @Test
  public void test01() throws SQLException {
    PreparedStatement s1 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    PreparedStatement s2 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    assertSame(s1, s2);
  }
  
  @Test
  public void test02() throws SQLException {
    Query q = getDB().select().columns("*").from(table.getName());
    PreparedStatement s1 = q.getQueryStatement();
    PreparedStatement s2 = compile(q.getSQLForQuery());
    assertSame(s1, s2);
  }
  
  @Test
  public void test03() throws SQLException {
    Query q1 = getDB().select().columns("*").from(table.getName());
    Query q2 = getDB().select().columns("*").from(table.getName()).where("login='foo'");
    Query q3 = getDB().select().columns("*").from(table.getName());
    PreparedStatement s1 = q1.getQueryStatement();
    PreparedStatement s2 = q2.getQueryStatement();
    PreparedStatement s3 = q3.getQueryStatement();
    assertNotSame(s1, s2);
    assertSame(s1, s3);
  }
  
  @Test
  public void test04() throws SQLException {
    Query q1 = getDB().select().columns("*").from(table.getName()).where("login='foo'");
    Query q2 = getDB().select().columns("*").from(table.getName()).where("login='foo'");
    PreparedStatement s1 = q1.getQueryStatement();
    PreparedStatement s2 = q2.getQueryStatement();
    assertSame(s1, s2);
  }

  @Test
  public void test05() throws SQLException {
    getDB().disableStatementPooling();
    Query q1 = getDB().select().columns("*").from(table.getName()).where("login='foo'");
    Query q2 = getDB().select().columns("*").from(table.getName()).where("login='foo'");
    PreparedStatement s1 = q1.getQueryStatement();
    PreparedStatement s2 = q2.getQueryStatement();
    assertNotSame(s1, s2);
  }
}
