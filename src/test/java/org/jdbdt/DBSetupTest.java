package org.jdbdt;

import static org.jdbdt.JDBDT.*;

import java.sql.Date;
import java.sql.SQLException;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DBSetupTest extends DBTestCase {

  @Rule public TestName testName = new TestName();


  private static Table table;

  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = 
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
    DataSet dataSet = data(table);
    for (User u : users) {
      dataSet.row(getConversion().convert(u));
    }
    int nExpected = users.length;
    if (populate) {
      populate(dataSet);
      assertSame(dataSet,table.getSnapshot());
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
  @Test @Category(TruncateSupportEnabled.class)
  public void testTruncate() throws SQLException {
    truncate(table);
    assertEquals(0, getDAO().count());
  }

  @Test
  public void testDeleteAll1() throws SQLException {
    deleteAll(table);
    assertEquals(0, getDAO().count());
  }
  
  @Test
  public void testDeleteAll2() throws SQLException {
    int n = getDAO().count();
    deleteAll(selectFrom(table).where("LOGIN='" + EXISTING_DATA_ID1+"'"));
    assertEquals(n-1, getDAO().count());
    assertNull(getDAO().query(EXISTING_DATA_ID1));
  }
  
  @Test
  public void testDeleteAll3() throws SQLException {
    int n = getDAO().count();
    deleteAll(selectFrom(table).where("LOGIN=?").withArguments(EXISTING_DATA_ID1));
    assertEquals(n-1, getDAO().count());
    assertNull(getDAO().query(EXISTING_DATA_ID1));
  }
  
  @Test
  public void testDeleteAll4() throws SQLException {
    int n = getDAO().count();
    deleteAll(selectFrom(table).where("LOGIN='" +EXISTING_DATA_ID1 + "'"));
    assertEquals(n-1, getDAO().count());
    assertNull(getDAO().query(EXISTING_DATA_ID1));
  }
  
  @Test(expected=InvalidUsageException.class)
  public void testDeleteAll5() throws SQLException {
    deleteAll(selectFrom(table));
  }
  
  @Test(expected=InvalidUsageException.class)
  public void testDeleteAll7() throws SQLException {
    deleteAll(selectFrom(table).where("w").groupBy("g"));
  }
  
  @Test(expected=InvalidUsageException.class)
  public void testDeleteAll8() throws SQLException {
    deleteAll(selectFrom(table).where("w").having("h"));
  }
}
