package org.jdbdt;

import static org.junit.Assert.*;
import static org.jdbdt.JDBDT.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {
  
  static final User[] INITIAL_DATA = {
    new User("linus", "Linus Torvalds", "linux", Date.valueOf("2015-01-01")),
    new User("steve", "Steve Hobs", "macos", Date.valueOf("2015-12-31")),
    new User("bill", "Bill Gates", "windows", Date.valueOf("2015-09-12"))
  };
  static User EXISTING_USER = INITIAL_DATA[1];
  static Connection conn;
  static TypedTable<User> table;
  static UserDAO sut;

  @BeforeClass
  public static void globalSetup() throws Exception {
    Class.forName("org.hsqldb.jdbcDriver");
    conn = DriverManager.getConnection("jdbc:hsqldb:mem:jdbdt-intg-test;shutdown=true");
    conn.setAutoCommit(true);
    sut = new UserDAO(conn);
    table = 
        table(UserDAO.TABLE_NAME,User.CONVERSION)
        .columns(UserDAO.COLUMNS)
        .boundTo(conn);
  }

  @AfterClass
  public static void globalTeardown() throws SQLException {
    conn.close();
  }

  TypedObserver<User> obs;

  @Before
  public void setUp() throws Exception {
    insertInto(table).rows(INITIAL_DATA);
    obs = observe(table);
  }

  @After
  public void tearDown() throws Exception {
    truncate(table);
  }

  @Test
  public void testUserInsertion() throws SQLException {
    User u = new User("new user", "Name", "pass", Date.valueOf("2015-01-01"));
    sut.doInsert(u);
    verify(obs).after(u);
  }

  @Test
  public void testInvalidUserInsertion()  {
    User u = new User(null, "Name", "pass", Date.valueOf("2015-01-01"));
    try {
      sut.doInsert(u);
      fail("Expected SQLException");
    }
    catch (SQLException e) {
      assertNoChanges(obs);
    }
  }

  @Test
  public void testUserRemoval() throws SQLException {
    int n = sut.doDelete(EXISTING_USER.getLogin());
    assertEquals(1, n);
    verify(obs).before(EXISTING_USER);
  }

  @Test
  public void testInvalidUserRemoval() throws SQLException {
    int n = sut.doDelete("NoSuchUser");
    assertEquals(0, n);
    assertNoChanges(obs);
  }

  @Test
  public void testUserUdate() throws SQLException {
    User u = EXISTING_USER.clone();
    u.setPassword("new password");
    int n = sut.doUpdate(u);
    assertEquals(1, n);
    verify(obs)
      .before(EXISTING_USER)
      .after(u);
  }
  @Test
  public void testNonUserUpdate() throws SQLException {
    User u = EXISTING_USER.clone();
    u.setLogin(u.getLogin()+"#");
    int n = sut.doUpdate(u);
    assertEquals(0, n);
    assertNoChanges(obs);
  }
  
  @Test
  public void testInvalidUserUpdate() {
    try {
      User u = EXISTING_USER.clone();
      u.setPassword(null);
      sut.doUpdate(u);
      fail("Expected " + SQLException.class);
    }
    catch (SQLException e) {
      assertNoChanges(obs);
    }
  }

}
