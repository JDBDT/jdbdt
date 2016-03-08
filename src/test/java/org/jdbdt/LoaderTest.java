package org.jdbdt;

import static org.jdbdt.JDBDT.insertInto;
import static org.jdbdt.JDBDT.table;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Date;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoaderTest extends DBTestCase {

  @Rule public TestName testName = new TestName();


  private static Table userTable;
  
  @BeforeClass
  public static void globalSetup() throws SQLException {
    userTable = 
        table(UserDAO.TABLE_NAME)
        .columns(UserDAO.COLUMNS)
        .boundTo(getConnection());
  }
  
  private static int newUserCounter = 0;
  
  private static User createNewUser() {
    final int unique = newUserCounter++;
    return new User("newUser" + unique, 
                       "New User" + unique, 
                       "pass" + unique, 
                       Date.valueOf("2015-01-01"));
  }
  private static Object[] dataFor(User u) {
    return getConversion().convert(u);
  }

  @Test 
  public void testInsert1() throws SQLException {
    User u = createNewUser();
    insertInto(userTable).row(dataFor(u));
    assertEquals(u, getDAO().query(u.getLogin()));
  }

  @Test 
  public void testInsert2() throws SQLException {
    User u1 = createNewUser(),
         u2 = createNewUser();
    insertInto(userTable).row(dataFor(u1)).row(dataFor(u2));
    assertEquals(u1, getDAO().query(u1.getLogin()));
    assertEquals(u2, getDAO().query(u2.getLogin()));
  }
  
  @Test 
  public void testInsert3() throws SQLException {
    try {
      insertInto(userTable).row(dataFor(getTestData(EXISTING_DATA_ID1)));
      fail("expected " + SQLException.class);
    } 
    catch(SQLException e) {}
  }

}
