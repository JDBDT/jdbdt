package org.jdbdt;


import static org.jdbdt.JDBDT.assertNoChanges;
import static org.jdbdt.JDBDT.logErrorsTo;
import static org.jdbdt.JDBDT.snapshot;
import static org.jdbdt.JDBDT.selectFrom;
import static org.jdbdt.JDBDT.table;
import static org.jdbdt.JDBDT.delta;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class TypedDeltaTest extends DBTestCase {


  @Rule public TestName testName = new TestName();
  private static final boolean DEBUG = false;
  
  private TypedSnapshot<User> theSUT;

  private static TypedTable<User> table;

  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = 
        table(UserDAO.TABLE_NAME, getConversion())
        .columns(UserDAO.COLUMNS)
        .boundTo(getConnection());
  }
  
  @Parameters
  public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {     
               { null, null }, 
               { "LOGIN LIKE '"+ EXISTING_DATA_ID1 + "%'", null }, 
               { "LOGIN LIKE ?",  new Object[] { EXISTING_DATA_ID1 + "%"} }  
         });
  }

  private final String whereClause;
  private final Object[] queryArgs;
  
  public TypedDeltaTest(String whereClause,Object[] queryArgs)  {
     this.whereClause = whereClause;
     this.queryArgs = queryArgs;
  }

  @Before 
  public void createObserver() throws SQLException {
    if (whereClause == null) {
      theSUT = snapshot(table);
    } else {
      theSUT = snapshot(selectFrom(table).where(whereClause), queryArgs);
    }
    if (DEBUG)
      logErrorsTo(System.err);
  }


  @Test
  public void testNoChanges() {
    delta(theSUT).end();
  }

  @Test
  public void testNoChanges2() {
    assertNoChanges(theSUT);
  }

  @Test(expected=DeltaAssertionError.class)
  public void testFailureInsertCase() throws SQLException {
    getDAO().doInsert(new User(EXISTING_DATA_ID1 + "_", "New User", "pass", Date.valueOf("2099-01-01")));
    assertNoChanges(theSUT);
  }

  @Test(expected=DeltaAssertionError.class)
  public void testFailureDeleteCase() throws SQLException {
    getDAO().doDelete(EXISTING_DATA_ID1);
    assertNoChanges(theSUT);
  }

  @Test(expected=DeltaAssertionError.class)
  public void testFailureUpdateCase() throws SQLException {
    getDAO().doUpdate(new User(EXISTING_DATA_ID1, "new name", "new password", Date.valueOf("2099-01-01")));
    assertNoChanges(theSUT);
  }

  @Test
  public void testSuccessInsertCase() throws SQLException {
    User u = new User(EXISTING_DATA_ID1 + "_", "New User", "pass", Date.valueOf("2099-01-01"));
    getDAO().doInsert(u);
    delta(theSUT)
    .after(u)
    .end();
  }

  @Test
  public void testSuccessInsertCaseList() throws SQLException {
    User u = new User(EXISTING_DATA_ID1 + "_", "New User", "pass", Date.valueOf("2099-01-01"));
    getDAO().doInsert(u);
    delta(theSUT)
    .after(Arrays.asList(u))
    .end();
  }
  @Test
  public void testSuccessDeleteCase() throws SQLException {
    User u = getTestData(EXISTING_DATA_ID1);
    getDAO().doDelete(EXISTING_DATA_ID1);
    delta(theSUT)
    .before(u)
    .end();
  }
  
  @Test
  public void testSuccessDeleteCaseList() throws SQLException {
    User u = getTestData(EXISTING_DATA_ID1);
    getDAO().doDelete(EXISTING_DATA_ID1);
    delta(theSUT)
    .before(Arrays.asList(u))
    .end();
  }

  @Test
  public void testSuccessUpdateCase() throws SQLException {
    User u1 = getDAO().query(EXISTING_DATA_ID1);
    User u2 = new User(EXISTING_DATA_ID1, "new name", "new password", Date.valueOf("2099-11-11"));
    getDAO().doUpdate(u2);
    delta(theSUT)
    .before(u1)
    .after(u2)
    .end();
  }

}
