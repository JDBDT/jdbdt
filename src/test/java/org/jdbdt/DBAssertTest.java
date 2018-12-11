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

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.jdbdt.TestUtil.expectAssertionError;
import static org.jdbdt.TestUtil.expectException;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DBAssertTest extends DBTestCase {

  private static final String ERROR_MSG = "assertion error";
  private static final String EMPTY_MSG = "";

  private  Table table;
  private Query query;

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
  
  private DataSet initialUsers;
  private DataSet initialLogins;
  private DataSet exUserDs;
  private DataSet exUserLDs;
  private DataSet newUserDs;
  private DataSet newUserLDs;
  private DataSet updUserDs;

  @Before 
  public void takeDBSnapshot() {
    table = table(UserDAO.TABLE_NAME)
        .columns(UserDAO.COLUMNS)
        .build(getDB());
    query = select("LOGIN")
           .from(table)
           .build(getDB());
    initialUsers = takeSnapshot(table);
    initialLogins = takeSnapshot(query);
    exUserDs = d(Actions.EXISTING_USER);
    newUserDs = d(Actions.USER_TO_INSERT);
    updUserDs = d(Actions.EXISTING_USER_UPDATED);
    exUserLDs = d(Actions.EXISTING_USER.getLogin());
    newUserLDs = d(Actions.USER_TO_INSERT.getLogin());
  }
  
  DataSet d(User... users) {
    return data(table, getConversion()).rows(users);
  }
  
  DataSet d(String... logins) {
    DataSet dataSet = data(query);
    for (String login : logins) {
      dataSet.row(login);
    }
    return dataSet;
  }

  @Test
  public void testNop() {
    Actions.nop();
    
    assertUnchanged(table);
    assertUnchanged(ERROR_MSG, query);
    assertUnchanged(table, query);
    assertUnchanged(ERROR_MSG, table, query);   
    
    assertDelta(empty(table), empty(table));
    assertDelta(ERROR_MSG, empty(query), empty(query));
    expectAssertionError(EMPTY_MSG, () -> assertDelta(exUserDs,updUserDs));
    expectAssertionError(ERROR_MSG, () -> assertDelta(ERROR_MSG, exUserDs,updUserDs));
    
    expectAssertionError(EMPTY_MSG, () -> assertInserted(exUserDs));
    expectAssertionError(ERROR_MSG, () -> assertInserted(ERROR_MSG, exUserLDs));
    expectAssertionError(EMPTY_MSG, () -> assertInserted(exUserDs,exUserLDs));
    expectAssertionError(ERROR_MSG, () -> assertInserted(ERROR_MSG, exUserDs,exUserLDs));
    
    expectAssertionError(EMPTY_MSG, () -> assertDeleted(exUserDs));
    expectAssertionError(ERROR_MSG, () -> assertDeleted(ERROR_MSG, exUserLDs));
    expectAssertionError(EMPTY_MSG, () -> assertDeleted(exUserDs,exUserLDs));
    expectAssertionError(ERROR_MSG, () -> assertDeleted(ERROR_MSG, exUserDs,exUserLDs));

    assertState(initialUsers);
    assertState(ERROR_MSG, initialLogins);
    assertState(initialUsers, initialLogins);
    assertState(ERROR_MSG, initialUsers, initialLogins);
    expectAssertionError(EMPTY_MSG, () -> assertEmpty(table));
    expectAssertionError(ERROR_MSG, () -> assertEmpty(ERROR_MSG, query));
    expectAssertionError(EMPTY_MSG, () -> assertEmpty(table, query));
    expectAssertionError(ERROR_MSG, () -> assertEmpty(ERROR_MSG, table, query));
  }
  
  @Test
  public void testDeleteAll() throws SQLException {
    Actions.deleteAll();
    
    expectAssertionError(EMPTY_MSG, () -> assertUnchanged(table));
    expectAssertionError(ERROR_MSG, () -> assertUnchanged(ERROR_MSG, query));
    expectAssertionError(EMPTY_MSG, () -> assertUnchanged(table, query));
    expectAssertionError(ERROR_MSG, () -> assertUnchanged(ERROR_MSG, table, query));
    
    assertDelta(initialUsers, empty(table));
    assertDelta(ERROR_MSG, initialLogins, empty(query));
   
    assertDeleted(initialUsers);
    assertDeleted(ERROR_MSG, initialLogins);
    assertDeleted(initialLogins, initialUsers);
    assertDeleted(ERROR_MSG, initialLogins, initialUsers);
    
    assertState(empty(table));
    assertState(ERROR_MSG, empty(query));
    assertState(empty(table), empty(query));
    assertState(ERROR_MSG, empty(table), empty(query));
    
    assertEmpty(table);
    assertEmpty(ERROR_MSG, query);
    assertEmpty(table, query);
    assertEmpty(ERROR_MSG, table, query);
  }
  
  @Test
  public void testDelete() throws SQLException {
    Actions.deleteUser();
    
    assertDelta(exUserDs, empty(table));
    assertDelta(ERROR_MSG, exUserLDs, empty(query));
   
    assertDeleted(exUserDs);
    assertDeleted(ERROR_MSG, exUserLDs);
    assertDeleted(exUserDs, exUserLDs);
    assertDeleted(ERROR_MSG, exUserDs, exUserLDs);
  }
  
  @Test
  public void testInsert() throws SQLException {
    Actions.insertNewUser();
    
    assertDelta(empty(table), newUserDs);
    assertDelta(ERROR_MSG,empty(query), newUserLDs);
   
    assertInserted(newUserDs);
    assertInserted(ERROR_MSG, newUserDs);
    assertInserted(newUserDs, newUserLDs);
    assertInserted(ERROR_MSG, newUserDs, newUserLDs);
    
    assertState(DataSet.join(initialUsers, newUserDs),
                DataSet.join(initialLogins, newUserLDs));
  }
  
  @Test
  public void testUpdate() throws SQLException {
    Actions.updateUser();
    assertDelta(exUserDs, updUserDs);
    assertUnchanged(ERROR_MSG, query);
  }
  
  @Test
  public void testRepeatedDataSets() throws SQLException {
    expectException(InvalidOperationException.class, () -> assertUnchanged(table, table));
    expectException(InvalidOperationException.class, () -> assertEmpty(table, table));
    expectException(InvalidOperationException.class, () -> assertState(empty(table), empty(table)));
    expectException(InvalidOperationException.class, () -> assertInserted(newUserDs, newUserDs));
    expectException(InvalidOperationException.class, () -> assertDeleted(newUserDs, newUserDs));
  }
}
