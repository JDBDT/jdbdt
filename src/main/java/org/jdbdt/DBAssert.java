/*
 * The MIT License
 *
 * Copyright (c) 2016-2019 Eduardo R. B. Marques
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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * Utility class with methods for assertion execution.
 * 
 * @since 1.0
 * 
 */
final class DBAssert {
  /**
   * Perform a delta assertion.
   * 
   * <p>
   * Delta assertion methods in the {@link JDBDT} delegate the
   * actual verification to this method.
   * </p>
   * 
   * @param callInfo Call info.
   * @param oldData Old data expected.
   * @param newData New data expected.
   * @throws DBAssertionError If the assertion fails.
   * @throws InvalidOperationException If the arguments are invalid. 
   */
  static void deltaAssertion(CallInfo callInfo, DataSet oldData, DataSet newData) {
    validateDeltaAssertion(oldData, newData);
    final DataSource source = oldData.getSource();
    final DB db = source.getDB();
    final DataSet snapshot = source.getSnapshot();
    final DataSet stateNow = source.executeQuery(callInfo, false);
    final Delta dbDelta = new Delta(snapshot, stateNow);
    final Delta oldDataMatch 
      = new Delta(oldData.getRows().iterator(), dbDelta.deleted());
    final Delta newDataMatch 
      = new Delta(newData.getRows().iterator(), dbDelta.inserted());
    final DeltaAssertion da = 
      new DeltaAssertion(oldData, newData, oldDataMatch, newDataMatch);
    db.log(callInfo, da);
    source.setDirtyStatus(!da.passed() || oldData.size() > 0 || newData.size() > 0);
    if (!da.passed()) {
      throw new DBAssertionError(callInfo.getMessage());
    }
  }
  
  @SuppressWarnings("javadoc")
  private static void
  validateDeltaAssertion(DataSet oldData, DataSet newData) {
    if (oldData == null) {
      throw new InvalidOperationException("Null argument for 'old' data set.");
    }
    if (newData == null) {
      throw new InvalidOperationException("Null argument for 'new' data set.");
    }
    if (oldData.getSource() != newData.getSource()) {
      throw new InvalidOperationException("Data source mismatch between data sets.");
    }
    if (oldData.getSource().getSnapshot() == null) {
      throw new InvalidOperationException("Undefined snapshot for data source.");
    } 
  }
  
  /**
   * Perform a database state assertion.
   * 
   * @param callInfo Call info.
   * @param expected Expected data.
   * @throws DBAssertionError If the assertion fails.
   * @throws InvalidOperationException If the arguments are invalid. 
   */
  static void stateAssertion(CallInfo callInfo, DataSet expected) {
    DataSource source =  expected.getSource();
    source.setDirtyStatus(true);
    dataSetAssertion(callInfo, 
                     expected,   
                     source.executeQuery(callInfo, false));
  }
  
  /**
   * Perform a data set assertion.
   * 
   * @param callInfo Call info.
   * @param expected Expected data.
   * @param actual Actual data.
   * @throws DBAssertionError If the assertion fails.
   * @throws InvalidOperationException If the arguments are invalid. 
   */
  static void dataSetAssertion(CallInfo callInfo, DataSet expected, DataSet actual) {
    validateDataSetAssertion(expected, actual);
    DataSource source = expected.getSource();
    Delta delta = new Delta(expected, actual); 
    DataSetAssertion assertion = new DataSetAssertion(expected, delta);
    source.getDB().log(callInfo, assertion);
    
    if (! assertion.passed()) {
      throw new DBAssertionError(callInfo.getMessage());
    }
  }

  @SuppressWarnings("javadoc")
  private static void
  validateDataSetAssertion(DataSet expected, DataSet actual) {
    if (expected == null) {
      throw new InvalidOperationException("Null argument for 'old' data set.");
    }
    if (actual == null) {
      throw new InvalidOperationException("Null argument for 'new' data set.");
    }
    if (expected.getSource() != actual.getSource()) {
      throw new InvalidOperationException("Data source mismatch between data sets.");
    }
  }
  
  /**
   * Assert if table exists or not.
   * 
   * @param callInfo Call info.
   * @param db Database.
   * @param tableName Table.
   * @param expected Expect if table exists or not.
   */
  static void assertTableExistence(CallInfo callInfo, DB db, String tableName, boolean expected) {
    boolean actual = tableExists(callInfo, db, tableName);
    SimpleAssertion assertion = new SimpleAssertion(null, expected, actual);
    db.log(callInfo, assertion);
    if (!assertion.passed()) {
      throw new DBAssertionError(callInfo.getMessage());
    }
  }
  
  /**
   * Check if table exists.
   * @param callInfo Call info.
   * @param db Database.
   * @param tableName Table name.
   * @return <code>true<code> if and only if the table exists.
   */
  private static boolean tableExists(CallInfo callInfo, DB db, String tableName) {
    return db.access(callInfo, () -> { 
      DatabaseMetaData dbmd = db.getConnection().getMetaData();
      try(ResultSet rs = dbmd.getTables(null, null, null, new String[] {"TABLE"})) {
        while(rs.next()) { 
          if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
            return true;
          }
        }
      }
      return false;
    });
  }
  
  /**
   * Private constructor to prevent instantiation.
   */
  private DBAssert() {
    
  }
  
}
