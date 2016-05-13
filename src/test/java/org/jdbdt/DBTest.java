package org.jdbdt;

import static org.junit.Assert.*;
import static org.jdbdt.JDBDT.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("javadoc")
public class DBTest extends DBTestCase {

  private PreparedStatement compile(String sql) throws SQLException {
    return getDB().compile(sql);
  }

  @Test @Category(StatementReuseEnabled.class)
  public void testReuse1() throws SQLException {
    PreparedStatement s1 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    PreparedStatement s2 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    assertSame(s1, s2);
  }

  @Test @Category(StatementReuseEnabled.class)
  public void testReuse2() throws SQLException {
    getDB().disable(DB.Option.REUSE_STATEMENTS);
    PreparedStatement s1 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    PreparedStatement s2 = compile("SELECT * FROM " + UserDAO.TABLE_NAME);
    getDB().enable(DB.Option.REUSE_STATEMENTS);
    assertNotSame(s1, s2);
  }

  class SaveRestoreTestHelper implements AutoCloseable {
    User user;
    boolean autoCommitInitialSetting;

    SaveRestoreTestHelper(boolean enableAC) throws SQLException {
      autoCommitInitialSetting = getDB().getConnection().getAutoCommit();
      getDB().getConnection().setAutoCommit(enableAC);
      query();
    }
    
    @Override
    public void close() throws SQLException {
      getDB().getConnection().setAutoCommit(autoCommitInitialSetting);
    }
    
    void update(String name) throws SQLException {
      user.setName(name);
      getDAO().doUpdate(user);
    }
    String query() throws SQLException {
      user = getDAO().query(EXISTING_DATA_ID1);
      return user.getName();
    }
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testRestoreWithoutSavepoint() throws SQLException {
    restore(getDB());
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testSavepointWithAutoCommitOn() throws SQLException {
    try (SaveRestoreTestHelper h = new SaveRestoreTestHelper(true)) {
      save(getDB());
    }
  }
   
  @Test
  public void testSavepointRestore() throws SQLException {
    try (SaveRestoreTestHelper h = new SaveRestoreTestHelper(false)) {
      String originalName = h.query(); 
      String changedName = "Mr. " + originalName; 
      
      // Set save-point
      save(getDB());
      
      // Make changes, then restore
      h.update(changedName);
      String qAfterChange = h.query();
      restore(getDB());
      String qAfterRestore = h.query();
      
      // Assert that changes and restore were effective
      assertEquals(changedName, qAfterChange);
      assertEquals(originalName, qAfterRestore);
    }
  }
  
  @Test
  public void testSavepointRestoreTwice() throws SQLException {
    try (SaveRestoreTestHelper h = new SaveRestoreTestHelper(false)) {
      String originalName = h.query(); 
      String changedName1 = "Mr. " + originalName; 
      String changedName2 = "Mr. " + originalName; 
      
      // Set save-point
      save(getDB());
   
      // Make a change, then restore
      h.update(changedName1);
      restore(getDB());
      String qAfterRestore1 = h.query();
      
      // Make a second change, then restore
      h.update(changedName2);
      restore(getDB());
      String qAfterRestore2 = h.query();
      
      // Assert that both restores were effective
      assertEquals(originalName, qAfterRestore1);
      assertEquals(originalName, qAfterRestore2);
    }
  }
  
  @Test
  public void testSavepointDiscard() throws SQLException {
    try (SaveRestoreTestHelper h = new SaveRestoreTestHelper(false)) {
      String originalName = h.query(); 
      String changedName1 = "Mr. " + originalName; 
      String changedName2 = "Dr. " + originalName; 
      
      // Set save-point
      save(getDB());
      
      // Make changes, set save-point again
      h.update(changedName1);
      String qAfterChange = h.query();
      save(getDB());
      
      // Make changes, and restore
      h.update(changedName2);
      String qAfterChange2 = h.query();
      restore(getDB());
      String qAfterRestore = h.query();
      
      // Assert that data from first save-point was lost
      assertEquals(changedName1, qAfterRestore);
      assertEquals(changedName1, qAfterChange);
      assertEquals(changedName2, qAfterChange2);
    }
  }
  
  @Test
  public void testRestoreAfterCommit() throws SQLException {
    try (SaveRestoreTestHelper h = new SaveRestoreTestHelper(false)) {
      String originalName = h.query(); 
      String changedName = "Mr. " + originalName; 
      
      // Set save-point
      save(getDB());
      
      // Make changes and commit them
      h.update(changedName);
      String qAfterChange = h.query();
      commit(getDB());
      
      // Expect exception
      TestUtil.expectException
      (InvalidOperationException.class, 
       () -> restore(getDB()) );
      String qAfterRestoreAttempt = h.query();

      // Assert that change took place, and that restore
      // attempted failed
      assertEquals(changedName, qAfterChange);
      assertEquals(changedName, qAfterRestoreAttempt);
    }
  }
  
  @Test
  public void testRestoreAfterExternalCommit() throws SQLException {
    try (SaveRestoreTestHelper h = new SaveRestoreTestHelper(false)) {
      String originalName = h.query(); 
      String changedName = "Mr. " + originalName; 
      
      // Set save-point
      save(getDB());
      
      // Make changes and commit them "externally"
      h.update(changedName);
      String qAfterChange = h.query();
      getDB().getConnection().commit();
      
      // Expect exception
      TestUtil.expectException
      (DBExecutionException.class, 
       () -> restore(getDB()) );
      String qAfterRestoreAttempt = h.query();

      // Assert that change took place, and that restore
      // attempted failed
      assertEquals(changedName, qAfterChange);
      assertEquals(changedName, qAfterRestoreAttempt);
    }
  }
  
  static final int INTENSIVE_TEST_ITERATIONS = 100;
  
  @Test
  public void testIntensiveRestore() throws SQLException {
    try (SaveRestoreTestHelper h = new SaveRestoreTestHelper(false)) {
      String originalName = h.query();
      save(getDB());
      for (int i = 0; i < INTENSIVE_TEST_ITERATIONS; i++) {
        h.update(originalName + "_" + i);
        restore(getDB());
      }
      assertEquals(originalName, h.query());
    }
  } 
}
