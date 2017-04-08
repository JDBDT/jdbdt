package org.jdbdt;

import static org.jdbdt.JDBDT.*;

import java.sql.Date;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest extends DBTestCase {
  

  static Table table;
  
  static DataSet initialData;

  @BeforeClass
  public static void doSetup() throws SQLException {
    DBTestCase.useCustomInit();
    table = table(UserDAO.TABLE_NAME)
            .columns(UserDAO.COLUMNS)
            .build(getDB());
    DataSetBuilder dsb = 
      builder(table)
     .sequence("login", i -> "user"+ i, 1)
     .sequence("name", i -> "User " +i , 1)
     .sequence("password", i -> "pass_" + i , 1);
    
    if (DBCfg.getConfig().isDateSupported()) {
     dsb.sequence("created", Date.valueOf("2015-01-01"), 1);
    } else {
      dsb.sequence("created", Date.valueOf("2015-01-01").getTime(), DataSetBuilder.MILLIS_PER_DAY);
    }
    initialData = dsb.generate(10).data();
    // getDB().enableFullLogging();
    getDB().getConnection().setAutoCommit(true);
    populate(initialData);
    getDB().getConnection().setAutoCommit(false);
  }
  
  @AfterClass
  public static void restoreAutoCommit() throws SQLException {
    getDB().getConnection().setAutoCommit(true);
  }

  @Before
  public void saveDBState() {
    save(getDB());
  }
  
  @After
  public void restoreDBState() {
    restore(getDB());
  }
  
  @Test
  public void testInit1() {
    assertUnchanged(table);
  }
  
  @Test
  public void testInit2() {
    assertState(initialData);
  }
 
  
  @Test 
  public void testCleanup1() throws Exception {
    deleteAll(table);
    assertEmpty(table);
  }
   
  @Test 
  public void testCleanup2() throws Exception {
    deleteAll(table);
    assertDeleted(initialData);
  }
  
  @Test 
  public void testCleanup3() throws Exception {
    getDAO().doDeleteAll();
    assertEmpty(table);
  }
  
  @Test 
  public void testCleanup4() throws Exception {
    getDAO().doDeleteAll();
    assertDeleted(initialData);
  }
  
  @Test
  public void testUserInsertion1() throws SQLException {
    User u = new User("new user", "Name", "pass", Date.valueOf("2015-01-01"));
    getDAO().doInsert(u);
    assertInserted(data(table, getConversion()).row(u));
  }
  
  @Test
  public void testUserInsertion2() throws SQLException {
    User u = new User("new user", "Name", "pass",Date.valueOf("2015-01-01"));
    DataSet expected = DataSet.join(initialData, data(table, getConversion()).row(u));
    getDAO().doInsert(u);
    assertState(expected);
  }
  
  @Test
  public void testUserInsertion3() throws SQLException {
    String newUserLogin = "new user";
    User u = new User(newUserLogin, "Name", "pass",Date.valueOf("2015-01-01"));
    Query q = select("login")
             .from(table)
             .where("login=?")
             .arguments(newUserLogin)
             .build(getDB());
    takeSnapshot(q);
    getDAO().doInsert(u);
    assertInserted(data(q).row(newUserLogin));
  }

  @Test
  public void testUserInsertion4() throws SQLException {
    String newUserLogin = "new user";
    User u = new User(newUserLogin, "Name", "pass",Date.valueOf("2015-01-01"));
    Query q = select("login")
             .from(table)
             .where("login=?")
             .arguments(newUserLogin)
             .build(getDB());
    getDAO().doInsert(u);
    assertState(data(q).row(newUserLogin));
  }
  
  @Test
  public void testUserUpdate1() throws SQLException {
    User u1 = getDAO().query("user1");
    User u2 = u1.clone();
    u2.setPassword("new password");
    getDAO().doUpdate(u2);
    assertDelta(data(table, getConversion()).row(u1),
                data(table, getConversion()).row(u2));   
  }
  
  @Test
  public void testUserUpdate2() throws SQLException {
    User u1 = getDAO().query("user1");
    User u2 = u1.clone();
    u2.setPassword("new password");
    getDAO().doUpdate(u2);
    assertState(DataSet.last(initialData, initialData.size()-1)
                       .add(data(table, getConversion()).row(u2)));  
  }

  @Test
  public void testUserUpdate3() throws SQLException {
    User u1 = getDAO().query("user1");
    User u2 = u1.clone();
    u2.setPassword("new password");
    Query q = select("password")
             .from(table)
             .where("login=?")
             .arguments(u1.getLogin())
             .build(getDB());
    takeSnapshot(q);
    getDAO().doUpdate(u2);
    assertDelta(data(q).row(u1.getPassword()),
                data(q).row(u2.getPassword()));
  }

  @Test
  public void testUserUpdate4() throws SQLException {
    User u1 = getDAO().query("user1");
    User u2 = u1.clone();
    u2.setPassword("new password");
    Query q = select("password")
             .from(table)
             .where("login=?")
             .arguments(u1.getLogin())
             .build(getDB());
    getDAO().doUpdate(u2);
    assertState(data(q).row(u2.getPassword()));
  }
  
  @Test
  public void testUserRemoval1() throws SQLException {
    User u = getDAO().query("user1");
    getDAO().doDelete("user1");
    assertDeleted(data(table, getConversion()).row(u));
  }
  
  @Test
  public void testUserRemoval2() throws SQLException {
    getDAO().doDelete("user1");
    assertState(DataSet.last(initialData, initialData.size() - 1));
  }
  
  @Test
  public void testUserRemoval3() throws SQLException {
    User[] u = {
        getDAO().query("user1"),
        getDAO().query("user2"),
        getDAO().query("user3")
    };
    getDAO().doDelete("user1", "user2", "user3");
    assertDeleted(data(table, getConversion()).rows(u));
  }
  
  @Test
  public void testUserRemoval4() throws SQLException {
    getDAO().doDelete("user1", "user2", "user3");
    assertState(DataSet.last(initialData, initialData.size() - 3 ));
  }
  
  @Test
  public void testUserRemoval5() throws SQLException {
    User u = getDAO().query("user1");
    Query q = select("login")
             .from(table)
             .where("login=?")
             .arguments(u.getLogin())
             .build(getDB());
    takeSnapshot(q);
    getDAO().doDelete(u.getLogin());
    assertDeleted(data(q).row(u.getLogin()));
  }
  
  @Test
  public void testUserRemoval6() throws SQLException {
    User u = getDAO().query("user1");
    Query q = select("password")
             .from(table)
             .where("login=?")
             .arguments(u.getLogin())
             .build(getDB());
    getDAO().doDelete(u.getLogin());
    assertEmpty(q);
  }

}
