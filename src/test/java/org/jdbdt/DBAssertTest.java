package org.jdbdt;


import static org.jdbdt.JDBDT.*;


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
public class DBAssertTest extends DBTestCase {

  @Parameters
  public static Collection<Object[]> parameterData() {
    return Arrays.asList(new Object[][] {     
        { null, null }, 
        { "LOGIN LIKE '"+ EXISTING_DATA_ID1 + "%'", null }, 
        { "LOGIN LIKE ?",  new Object[] { EXISTING_DATA_ID1 + "%"} }  
    });
  }

  @Rule public TestName testName = new TestName();
  private static Table table;
  private DataSource dataSource;

  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = getDB().table(UserDAO.TABLE_NAME)
                   .columns(UserDAO.COLUMNS);
  }
  
  private final String whereClause;
  private final Object[] queryArgs;

  public DBAssertTest(String whereClause,Object[] queryArgs)  {
    this.whereClause = whereClause;
    this.queryArgs = queryArgs;
  }

  @Before 
  public void takeDBSnapshot() throws SQLException {
    if (whereClause == null) {
      dataSource = table;
    } else {
      Query q = getDB().select().from(table).where(whereClause);
      if (queryArgs != null) {
        q.withArguments(queryArgs);
      }
      dataSource = q;
    }
    takeSnapshot(dataSource);
  }


  @Test
  public void testNoChanges() {
    assertUnchanged(dataSource);
  }

  @Test(expected=DBAssertionError.class)
  public void testFailureInsertCase() throws SQLException {
    getDAO().doInsert(new User(EXISTING_DATA_ID1 + "_", "New User", "pass", Date.valueOf("2099-01-01")));
    assertUnchanged(dataSource);
  }

  @Test(expected=DBAssertionError.class)
  public void testFailureDeleteCase() throws SQLException {
    getDAO().doDelete(EXISTING_DATA_ID1);
    assertUnchanged(dataSource);
  }

  @Test(expected=DBAssertionError.class)
  public void testFailureUpdateCase() throws SQLException {
    getDAO().doUpdate(new User(EXISTING_DATA_ID1, "new name", "new password", Date.valueOf("2099-01-01")));
    assertUnchanged(dataSource);
  }

  @Test
  public void testSuccessInsertCase() throws SQLException {
    User u = new User(EXISTING_DATA_ID1 + "_", "New User", "pass", Date.valueOf("2099-01-01"));
    getDAO().doInsert(u);
    assertInserted(data(dataSource)
    .row(u.getLogin(), u.getName(), u.getPassword(), dateValue(u.getCreated())));
  }

  @Test
  public void testSuccessInsertCase2() throws SQLException {
    User u = new User(EXISTING_DATA_ID1 + "_", "New User", "pass", Date.valueOf("2099-01-01"));
    getDAO().doInsert(u);
    assertInserted( 
        data(dataSource)
        .row(u.getLogin(), 
            u.getName(), 
            u.getPassword(), 
            dateValue(u.getCreated()))
        );
  }

  @Test
  public void testSuccessDeleteCase() throws SQLException {
    User u = getTestData(EXISTING_DATA_ID1);
    getDAO().doDelete(EXISTING_DATA_ID1); 
    assertDeleted(
        data(dataSource)
        .row(EXISTING_DATA_ID1, 
             u.getName(), 
             u.getPassword(), 
             dateValue(u.getCreated())));
  }

  @Test
  public void testSuccessDeleteCase2() throws SQLException {
    User u = getTestData(EXISTING_DATA_ID1);
    getDAO().doDelete(EXISTING_DATA_ID1);      
    assertDeleted( 
        data(dataSource)
        .row(u.getLogin(), 
            u.getName(), 
            u.getPassword(), 
            dateValue(u.getCreated()))
        );
  }

  @Test
  public void testSuccessUpdateCase() throws SQLException {
    User u1 = getDAO().query(EXISTING_DATA_ID1);
    User u2 = new User(EXISTING_DATA_ID1, "new name", "new password", Date.valueOf("2099-11-11"));
    getDAO().doUpdate(u2);
    DataSet oldData = 
      data(dataSource)
        .row(EXISTING_DATA_ID1, u1.getName(), u1.getPassword(), dateValue(u1.getCreated()));
    DataSet newData = 
      data(dataSource)
        .row(EXISTING_DATA_ID1, u2.getName(), u2.getPassword(), dateValue(u2.getCreated()));
    assertDelta(oldData, newData);
  }
  
  @Test
  public void testSuccessUpdateCase2() throws SQLException {
    User u1 = getDAO().query(EXISTING_DATA_ID1);
    User u2 = new User(EXISTING_DATA_ID1, "new name", "new password", Date.valueOf("2099-11-11"));
    getDAO().doUpdate(u2);
    assertDelta( 
        data(dataSource)
        .row(u1.getLogin(), 
            u1.getName(), 
            u1.getPassword(), 
            dateValue(u1.getCreated())),
        data(dataSource)
        .row(u2.getLogin(), 
            u2.getName(), 
            u2.getPassword(), 
            dateValue(u2.getCreated()))
        ); 
  }
}
