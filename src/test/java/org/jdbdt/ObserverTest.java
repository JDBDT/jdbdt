package org.jdbdt;


import static org.jdbdt.JDBDT.assertNoChanges;
import static org.jdbdt.JDBDT.observe;
import static org.jdbdt.JDBDT.logErrorsTo;
import static org.jdbdt.JDBDT.selectFrom;
import static org.jdbdt.JDBDT.table;
import static org.jdbdt.JDBDT.verify;

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
public class ObserverTest extends DBTestCase {

  @Parameters
  public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {     
               { null, null }, 
               { "LOGIN LIKE '"+ EXISTING_DATA_ID1 + "%'", null }, 
               { "LOGIN LIKE ?",  new Object[] { EXISTING_DATA_ID1 + "%"} }  
         });
  }

  @Rule public TestName testName = new TestName();
  private static Table table;
  private Observer theSUT;
  
  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = table(UserDAO.TABLE_NAME)
           .columns(UserDAO.COLUMNS)
           .boundTo(getConnection());
  }
  private final String whereClause;
  private final Object[] queryArgs;
  
  public ObserverTest(String whereClause,Object[] queryArgs)  {
     this.whereClause = whereClause;
     this.queryArgs = queryArgs;
  }

  @Before 
  public void createObserver() throws SQLException {
    if (whereClause == null) {
      theSUT = observe(table);
    } else {
      theSUT = observe(selectFrom(table).where(whereClause), queryArgs);
    }
    logErrorsTo(System.err);
  }
  
  @Test
  public void testNoChanges() {
    verify(theSUT).end();
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
    verify(theSUT)
      .after(u.getLogin(), u.getName(), u.getPassword(), u.getCreated())
      .end();
  }
  
  @Test
  public void testSuccessDeleteCase() throws SQLException {
    User u = getTestData(EXISTING_DATA_ID1);
    getDAO().doDelete(EXISTING_DATA_ID1);
    verify(theSUT)
      .before(EXISTING_DATA_ID1, u.getName(), u.getPassword(), u.getCreated())
      .end();
  }
  
  @Test
  public void testSuccessUpdateCase() throws SQLException {
    User u1 = getDAO().query(EXISTING_DATA_ID1);
    User u2 = new User(EXISTING_DATA_ID1, "new name", "new password", Date.valueOf("2099-11-11"));
    getDAO().doUpdate(u2);
    verify(theSUT)
      .before(EXISTING_DATA_ID1, u1.getName(), u1.getPassword(), u1.getCreated())
      .after(EXISTING_DATA_ID1, u2.getName(), u2.getPassword(), u2.getCreated())
      .end();
  }
  
}
