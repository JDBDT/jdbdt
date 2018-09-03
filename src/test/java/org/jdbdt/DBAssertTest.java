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
import static org.junit.Assert.*;

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

  private static Table table;
  private DataSource dataSource;

  @BeforeClass
  public static void globalSetup() {
    table = table(UserDAO.TABLE_NAME)
           .columns(UserDAO.COLUMNS)
           .build(getDB());
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


  @Test 
  public void testTableExists1() {
    assertTableExists(getDB(), table.getName());
  }
  
  @Test(expected=DBAssertionError.class)
  public void testTableDoesNotExist1() throws SQLException {
    assertTableDoesNotExist(getDB(), table.getName());
  }
  
  @Test(expected=DBAssertionError.class)
  public void testTableExists2() throws SQLException {
    getDAO().dropTable();
    try {
      assertTableExists(getDB(), table.getName());
      fail("Expected " + DBAssertionError.class);
    }
    finally {
      getDAO().createTable();
    }   
  }
  
  @Test 
  public void testTableDoesNotExist2() throws SQLException {
    getDAO().dropTable();
    try {
      assertTableDoesNotExist(getDB(), table.getName());
    }
    finally {
      getDAO().createTable();
    }   
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
  
  @Test
  public void testStateAssertion1() {
    assertState(initialState);
  }
  
  @Test(expected=DBAssertionError.class)
  public void testStateAssertionFailure1() throws SQLException {
    getDAO().doDelete(EXISTING_DATA_ID1);
    assertState(initialState);
  }
  
  @Test
  public void testStateAssertion2() throws SQLException {
    getDAO().doDeleteAll();
    assertEmpty(dataSource);
  }
 
}
