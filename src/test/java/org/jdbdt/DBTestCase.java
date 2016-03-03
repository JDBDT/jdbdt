package org.jdbdt;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;


/**
 * Parent class for a database test classes.
 * 
 * @since 0.1
 */
@SuppressWarnings("javadoc")
public class DBTestCase {

  private static final String DB_URL = 
      "jdbc:hsqldb:mem:jdbdt-test;shutdown=true";

  private static Connection gConn;
  private static UserDAO gDAO;

  @BeforeClass
  public static void setupDB() throws Exception {
    gConn = DriverManager.getConnection(DB_URL);
    gConn.setAutoCommit(true);
    gDAO = new UserDAO(getConnection());
  }
  
  @AfterClass
  public static void teardownDB() throws SQLException {
    gConn.close();
  }

  protected static Connection getConnection() {
    return gConn;
  }
  
  protected static UserDAO getDAO() {
    return gDAO;
  }
  
  @Before
  public void setup() throws SQLException {
    getDAO().doDeleteAll();
    getDAO().doInsert(INITIAL_DATA);
  }
  
  
  protected static final User[] INITIAL_DATA = {
    new User("linus", "Linus Torvalds", "linux", Date.valueOf("2015-01-01")),
    new User("steve", "Steve Hobs", "macos", Date.valueOf("2015-12-31")),
    new User("bill", "Bill Gates", "windows", Date.valueOf("2015-09-12"))
  };
  
  protected static final String EXISTING_DATA_ID1 =
      INITIAL_DATA[0].getLogin();
  
  protected static final String EXISTING_DATA_ID2 =
      INITIAL_DATA[INITIAL_DATA.length/2].getLogin();
  
  protected static final String EXISTING_DATA_ID3 =
      INITIAL_DATA[INITIAL_DATA.length-1].getLogin();


  
  static {
    try {
      Class.forName("org.hsqldb.jdbcDriver");
    } catch(Throwable e) {
      throw new Error(e);
    }
  }
  
 
  protected static User getTestData(String id) {
    for (User u : INITIAL_DATA) {
      if (u.getLogin().equals(id)) {
        return u;
      }
    }
    return null;
  }
}
