package org.jdbdt;

import static org.jdbdt.JDBDT.*;


import java.sql.Date;
import java.sql.SQLException;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataInsertionTest extends DBTestCase {

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

  void doInsert(User... users) throws SQLException {
    executeTest(false, users);
  }

  void doPopulate(User... users) throws SQLException {
    executeTest(true, users);
  }
  
  void executeTest(boolean populate, User[] users) throws SQLException {
    DataSet dataSet = data(userTable);
    for (User u : users) {
      dataSet.row(getConversion().convert(u));
    }
    int nExpected = users.length;
    if (populate) {
      populate(dataSet);
      assertSame(dataSet,userTable.getSnapshot());
    } 
    else {
      nExpected += getDAO().count();
      insert(dataSet);
    }
    assertEquals(nExpected , getDAO().count());
    for (User u : users) {
      assertEquals(u, getDAO().query(u.getLogin()));
    }
  }
  
  @Test(expected=InvalidUsageException.class) 
  public void testInsert0() throws SQLException {
    doInsert(); // Empty data set
  }
  
  @Test(expected=InvalidUsageException.class) 
  public void testPopulate0() throws SQLException {
    doPopulate(); // Empty data set
  }

  @Test 
  public void testInsert1() throws SQLException {
    doInsert(createNewUser());  
  }
  
  @Test 
  public void testPopulate1() throws SQLException {
    doPopulate(createNewUser());  
  }

  @Test 
  public void testInsert2() throws SQLException {
    doInsert(createNewUser(), 
             createNewUser(), 
             createNewUser());
  }
  
  @Test 
  public void testPopulate2() throws SQLException {
    doPopulate(createNewUser(), 
             createNewUser(), 
             createNewUser());
  }

  @Test(expected=SQLException.class)
  public void testInsert3() throws SQLException {
    doInsert(getTestData(EXISTING_DATA_ID1));
  }
  
  @Test
  public void testPopulate3() throws SQLException {
    doPopulate(getTestData(EXISTING_DATA_ID1));
  }
}
