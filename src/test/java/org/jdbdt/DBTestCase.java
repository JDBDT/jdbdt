package org.jdbdt;

import java.sql.Connection;
import java.sql.Date;
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

  private static DB gDB;
  private static UserDAO gDAO;
  private static Conversion<User> gConversion;
  
  protected static final 
  Conversion<User> STD_CONVERSION = 
     u -> new Object[] {
        u.getLogin(), 
        u.getName(), 
        u.getPassword(), 
        u.getCreated()
     };
  // Conversion when DATE is not supported (e.g. for SQLite)
  protected static final 
  Conversion<User> ALT_CONVERSION = 
      u -> new Object[] {
         u.getLogin(), 
         u.getName(), 
         u.getPassword(), 
         u.getCreated() != null ? u.getCreated().getTime() : null
  };
  
  protected static Conversion<User> getConversion() {
    return gConversion;
  }
  protected static Object dateValue(Date d) {
    return gConversion == STD_CONVERSION ?  d : d.getTime();
  }
      
  @BeforeClass
  public static void setupDB() throws Exception {
    DBCfg cfg = DBCfg.getConfig();
    Class.forName(cfg.getDriver());
    gDB = JDBDT.database(cfg.getURL());
    Connection c = gDB.getConnection();
    c.setAutoCommit(true);
    gDAO = new UserDAO(c);
    gConversion = cfg.isDateSupported() ? STD_CONVERSION : ALT_CONVERSION;
    if (!cfg.reuseStatements()) {
      gDB.disable(DB.Option.REUSE_STATEMENTS);
    }
    newUserCounter = 0;
    gDB.disable(DB.Option.LOG_ASSERTION_ERRORS);
   // gDB.enableFullLogging();
  }
  
  @AfterClass
  public static void teardownDB() throws SQLException {
    JDBDT.teardown(gDB, true);
    useCustomInit = false;
  }

  protected static DB getDB() {
    return gDB;
  }
  
  protected static UserDAO getDAO() {
    return gDAO;
  }

  private static boolean useCustomInit = false;
  
  static void useCustomInit() {
    useCustomInit = true;
  }
 
  @Before
  public void populateByDefault() throws SQLException {
    if (!useCustomInit) {
      getDAO().doDeleteAll();
      getDAO().doInsert(INITIAL_DATA);
    }
  }
  
  
  protected static final User[] INITIAL_DATA = {
    new User("linus", "Linus Torvalds", "linux", Date.valueOf("2015-01-01")),
    new User("steve", "Steve Jobs", "macos", Date.valueOf("2015-12-31")),
    new User("bill", "Bill Gates", "windows", Date.valueOf("2015-09-12")),
    new User("alanis", "Alanis ", "xyz", Date.valueOf("2015-01-01")),
    new User("blanis", "Blanis ", "xyz", Date.valueOf("2015-01-02")),
    new User("clanis", "Clanis ", "xyz", Date.valueOf("2015-01-03")),
    new User("dlanis", "Dlanis ", "xyz", Date.valueOf("2015-01-04")),
    new User("elanis", "Elanis ", "xyz", Date.valueOf("2015-01-05")),
    new User("flanis", "Flanis ", "xyz", Date.valueOf("2015-01-06")),
    new User("glanis", "Glanis ", "xyz", Date.valueOf("2015-01-07")),
    new User("hlanis", "Hlanis ", "xyz", Date.valueOf("2015-01-08")),
    new User("ilanis", "Ilanis ", "xyz", Date.valueOf("2015-01-09")),
    new User("jlanis", "Jlanis ", "xyz", Date.valueOf("2015-01-10"))
  };
  
  protected static final String EXISTING_DATA_ID1 =
      INITIAL_DATA[0].getLogin();
  
  protected static final String EXISTING_DATA_ID2 =
      INITIAL_DATA[INITIAL_DATA.length/2].getLogin();
  
  protected static final String EXISTING_DATA_ID3 =
      INITIAL_DATA[INITIAL_DATA.length-1].getLogin();
 
  protected static User getTestData(String id) {
    for (User u : INITIAL_DATA) {
      if (u.getLogin().equals(id)) {
        return u;
      }
    }
    return null;
  }
  
  protected Object[] rowFor(User u) {
    return getConversion().convert(u);
  }
  
  private static int newUserCounter = 0;
  
  protected static User createNewUser() {
    final int unique = newUserCounter++;
    return new User("user" + unique, 
        "New User" + unique, 
        "pass" + unique, 
        Date.valueOf("2016-01-01"));
  }

}
