/*
 * The MIT License
 *
 * Copyright (c) Eduardo R. B. Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jdbdt;

import static org.junit.Assert.*;
import static org.jdbdt.JDBDT.*;

import java.sql.SQLException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("javadoc")
public class DBSavepointTest extends DBTestCase {

  static class SaveRestoreTestHelper implements AutoCloseable {
    User user;
    boolean autoCommitInitialSetting;

    SaveRestoreTestHelper(boolean enableAC) throws SQLException {
      autoCommitInitialSetting = getDB().getAutoCommit();
      getDB().setAutoCommit(enableAC);
      query();
    }
    
    @Override
    public void close() {
      getDB().setAutoCommit(autoCommitInitialSetting);
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
  
  @Test(expected=InvalidOperationException.class) @Category(TestCategories.Savepoints.class)
  public void testRestoreWithoutSavepoint() throws SQLException {
    restore(getDB());
  }
  
  @Test(expected=InvalidOperationException.class) @Category(TestCategories.Savepoints.class)
  public void testSavepointWithAutoCommitOn() throws SQLException {
    try (SaveRestoreTestHelper h = new SaveRestoreTestHelper(true)) {
      save(getDB());
    }
  }
   
  @Test @Category(TestCategories.Savepoints.class)
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
  
  @Test @Category(TestCategories.Savepoints.class)
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
      String qAfterRestore = h.query();
      
      // Make a second change, then restore
      h.update(changedName2);
      TestUtil.expectException(InvalidOperationException.class,
          () -> restore(getDB()));
      String qAfterFailedRestore = h.query();
      
      // Assert that both restores were effective
      assertEquals(originalName, qAfterRestore);
      assertEquals(changedName2, qAfterFailedRestore);
    }
  }
  
  @Test @Category(TestCategories.Savepoints.class)
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
  
  @Test @Category(TestCategories.Savepoints.class)
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
  
  @Test @Category(TestCategories.Savepoints.class)
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
  
  @Test @Category(TestCategories.Savepoints.class)
  public void testIntensiveSaveRestore() throws SQLException {
    final int INTENSIVE_TEST_ITERATIONS = 100;
    try (SaveRestoreTestHelper h = new SaveRestoreTestHelper(false)) {
      String originalName = h.query();
      for (int i = 0; i < INTENSIVE_TEST_ITERATIONS; i++) {
        save(getDB());
        h.update(originalName + "_" + i);
        restore(getDB());
      }
      assertEquals(originalName, h.query());
    }
  } 

}
