/*
 * The MIT License
 *
 * Copyright (c) 2016-2018 Eduardo R. B. Marques
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


import static org.jdbdt.JDBDT.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.jdbdt.TestUtil.expectAssertionError;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class DBAssertTest extends DBTestCase {

  @Parameters
  public static Collection<Object[]> parameterData() {
    return Arrays.asList(new Object[][] {     
        { null, null }, 
        { "LOGIN LIKE '"+ EXISTING_DATA_ID1 + "%'", new Object[0] }, 
        { "LOGIN LIKE ?",  new Object[] { EXISTING_DATA_ID1 + "%"} }  
    });
  }
  private static final String ERROR_MSG = "assertion error";
  private static final String EMPTY_MSG = "";

  private static Table table;
  private DataSource dataSource;

  @BeforeClass
  public static void globalSetup() {
    table = table(UserDAO.TABLE_NAME)
           .columns(UserDAO.COLUMNS)
           .build(getDB());
  }
  

  static class Actions {
    static final User EXISTING_USER = getTestData(EXISTING_DATA_ID1);
    static final User USER_TO_INSERT = new User(EXISTING_DATA_ID1 + "_", "New User", "pass", Date.valueOf("2099-01-01"));
    static final User EXISTING_USER_UPDATED = new User(EXISTING_DATA_ID1, "new name", "new password", Date.valueOf("2099-01-01"));
    
    static void nop() { }
    
    static void insertNewUser() throws SQLException {
      getDAO().doInsert(USER_TO_INSERT);
    }
    
    static void deleteUser() throws SQLException {
      getDAO().doDelete(EXISTING_DATA_ID1);
    }
    
    static void updateUser() throws SQLException {
      getDAO().doUpdate(EXISTING_USER_UPDATED);
    }
    
    static void deleteAll() throws SQLException {
      getDAO().doDeleteAll();
    }
  }
  
  private final String whereClause;
  private final Object[] queryArgs;
  private DataSet initialState;
  
  public DBAssertTest(String whereClause, Object[] queryArgs)  {
    this.whereClause = whereClause;
    this.queryArgs = queryArgs;
  }

  @Before 
  public void takeDBSnapshot() {
    if (whereClause == null) {
      dataSource = table;
    } else {
      dataSource = 
        select(UserDAO.COLUMNS)
       .from(table)
       .where(whereClause)
       .arguments(queryArgs)
       .build(getDB());
    }
    initialState = takeSnapshot(dataSource);
  }
  
  DataSet d(User... users) {
    DataSet dataSet = data(dataSource);
    for (User u : users) {
      dataSet.row(getConversion().convert(u));
    }
    return dataSet;
  }

  @Test
  public void testAssertUnchanged1() {
    Actions.nop();
    assertUnchanged(dataSource);
  }
  
  @Test
  public void testAssertUnchanged2() {
    Actions.nop();
    assertUnchanged(ERROR_MSG, dataSource);
  }

  @Test
  public void testAssertUnchanged3() throws SQLException {
    Actions.insertNewUser();
    expectAssertionError(EMPTY_MSG, () -> assertUnchanged(dataSource));
  }

  @Test
  public void testAssertUnchanged4() throws SQLException {
    Actions.deleteUser();
    expectAssertionError(ERROR_MSG, () -> assertUnchanged(ERROR_MSG, dataSource));
  }

  @Test
  public void testAssertUnchanged5() throws SQLException {
    Actions.updateUser();
    expectAssertionError(ERROR_MSG, () -> assertUnchanged(ERROR_MSG, dataSource));
  }
  
  @Test
  public void testAssertInserted1() throws SQLException {
    Actions.insertNewUser();
    assertInserted(d(Actions.USER_TO_INSERT));
  }

  @Test
  public void testAssertInserted2() throws SQLException {
    Actions.insertNewUser();
    assertInserted(ERROR_MSG, d(Actions.USER_TO_INSERT));
  }
  
  @Test
  public void testAssertInserted3() throws SQLException {
    Actions.nop();
    expectAssertionError(EMPTY_MSG, () -> assertInserted(d(Actions.EXISTING_USER)));
  }
  
  @Test
  public void testAssertInserted4() throws SQLException {
    Actions.deleteUser();
    expectAssertionError(ERROR_MSG, () -> assertInserted(ERROR_MSG, d(Actions.EXISTING_USER)));
  }
  
  @Test
  public void testDelete1() throws SQLException {
    Actions.deleteUser();
    assertDeleted(d(Actions.EXISTING_USER));
  }
  
  @Test
  public void testDelete2() throws SQLException {
    Actions.deleteUser();
    assertDeleted(ERROR_MSG, d(Actions.EXISTING_USER));
  }
  
  @Test
  public void testDelete3() throws SQLException {
    Actions.nop();
    expectAssertionError(EMPTY_MSG, () -> assertDeleted(d(Actions.EXISTING_USER)));
  }
  
  @Test
  public void testDelete4() throws SQLException {
    Actions.insertNewUser();
    expectAssertionError(ERROR_MSG, () -> assertDeleted(ERROR_MSG, d(Actions.EXISTING_USER)));
  }

  @Test
  public void testAssertDelta1() throws SQLException {
    Actions.updateUser();
    assertDelta(d(Actions.EXISTING_USER), d(Actions.EXISTING_USER_UPDATED));
  }
  
  @Test
  public void testAssertDelta2() throws SQLException {
    Actions.updateUser();
    assertDelta(ERROR_MSG, d(Actions.EXISTING_USER), d(Actions.EXISTING_USER_UPDATED));
  }
  
  @Test
  public void testAssertDelta3() throws SQLException {
    Actions.nop();
    expectAssertionError(EMPTY_MSG, () -> assertDelta(d(Actions.EXISTING_USER), d(Actions.EXISTING_USER_UPDATED)));
  }
  
  @Test
  public void testAssertDelta4() throws SQLException {
    Actions.insertNewUser();
    expectAssertionError(ERROR_MSG, () -> assertDelta(ERROR_MSG, d(Actions.EXISTING_USER), d(Actions.EXISTING_USER_UPDATED)));
  }
  
  @Test
  public void testAssertState1() {
    Actions.nop();
    assertState(initialState);
  }
  
  @Test
  public void testAssertState2() {
    Actions.nop();
    assertState(ERROR_MSG, initialState);
  }
  
  @Test
  public void testAssertState3() throws SQLException {
    Actions.insertNewUser();
    expectAssertionError(EMPTY_MSG, () -> assertState(initialState));
  }
  
  @Test
  public void testAssertState4() throws SQLException {
    Actions.deleteUser();
    expectAssertionError(ERROR_MSG, () -> assertState(ERROR_MSG, initialState));
  }
  
  @Test
  public void testAssertState5() throws SQLException {
    Actions.updateUser();
    expectAssertionError(ERROR_MSG, () -> assertState(ERROR_MSG, initialState));
  }
  
  @Test
  public void testAssertEmpty1() throws SQLException {
    Actions.deleteAll();
    assertEmpty(dataSource);
  }
  
  @Test
  public void testAssertEmpty2() throws SQLException {
    Actions.deleteAll();
    assertEmpty("empty", dataSource);
  }
 
  @Test
  public void testAssertEmpty3() throws SQLException {
    Actions.nop();
    expectAssertionError(EMPTY_MSG, () -> assertEmpty(dataSource));
  }
  
  @Test
  public void testAssertEmpty4() throws SQLException {
    Actions.insertNewUser();
    expectAssertionError(ERROR_MSG, () -> assertEmpty(ERROR_MSG, dataSource));
  }
}
