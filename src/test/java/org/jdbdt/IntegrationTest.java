package org.jdbdt;

import static org.jdbdt.JDBDT.assertNoChanges;
import static org.jdbdt.JDBDT.deleteAll;
import static org.jdbdt.JDBDT.delta;
import static org.jdbdt.JDBDT.observe;
import static org.jdbdt.JDBDT.table;
import static org.jdbdt.JDBDT.truncate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest extends DBTestCase{
  

  static TypedTable<User> table;
  
  TypedObserver<User> obs;

  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = table(UserDAO.TABLE_NAME, getConversion()).boundTo(getConnection());
  }
  @Before
  public void setUp() throws Exception {
    obs = observe(table);
  }

  @Test @Category(TruncateSupportEnabled.class)
  public void testTruncate() throws Exception {
    truncate(table);
    assertEquals(0, getDAO().count());
  }

  @Test @Category(TruncateSupportEnabled.class)
  public void testDeleteAll() throws Exception {
    deleteAll(table);
    assertEquals(0, getDAO().count());
  }
  
  @Test
  public void testUserInsertion1() throws SQLException {
    User u = new User("new user", "Name", "pass",Date.valueOf("2015-01-01"));
    getDAO().doInsert(u);
    delta(obs).after(u).end();
  }

  @Test
  public void testUserInsertion2() throws SQLException {
    User u = new User("new user", "Name", "pass", null);
    getDAO().doInsert(u);
    delta(obs).after(u).end();
  }

  @Test
  public void testInvalidUserInsertion()  {
    User u = new User(null, "Name", "pass", Date.valueOf("2015-01-01"));
    try {
      getDAO().doInsert(u);
      fail("Expected SQLException");
    }
    catch (SQLException e) {
      assertNoChanges(obs);
    }
  }

  @Test
  public void testUserRemoval() throws SQLException {
    User u1 = getDAO().query(EXISTING_DATA_ID1);
    User u2 = getDAO().query(EXISTING_DATA_ID2);
    User u3 = getDAO().query(EXISTING_DATA_ID3);

    int n = getDAO().doDelete(EXISTING_DATA_ID1, 
                         EXISTING_DATA_ID2, 
                         EXISTING_DATA_ID3);
    assertEquals(3, n);
    delta(obs).before(Arrays.asList(u1, u2, u3)).end();
  }

  @Test
  public void testInvalidUserRemoval() throws SQLException {
    int n = getDAO().doDelete("NoSuchUser");
    assertEquals(0, n);
    assertNoChanges(obs);
  }

  @Test
  public void testUserUdate() throws SQLException {
    User u1 = getDAO().query(EXISTING_DATA_ID1);
    User u2 = u1.clone();
    u2.setPassword("new password");
    int n = getDAO().doUpdate(u2);
    assertEquals(1, n);
    delta(obs)
      .before(u1)
      .after(u2)
      .end();
  }
  @Test
  public void testNonUserUpdate() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1).clone();
    u.setLogin(u.getLogin()+"#");
    int n = getDAO().doUpdate(u);
    assertEquals(0, n);
    assertNoChanges(obs);
  }
  
  @Test
  public void testInvalidUserUpdate() {
    try {
      User u = getDAO().query(EXISTING_DATA_ID1).clone();
      u.setPassword(null);
      getDAO().doUpdate(u);
      fail("Expected " + SQLException.class);
    }
    catch (SQLException e) {
      assertNoChanges(obs);
    }
  }

}
