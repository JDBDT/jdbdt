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


import static org.jdbdt.JDBDT.assertTableDoesNotExist;
import static org.jdbdt.JDBDT.assertTableExists;
import static org.jdbdt.JDBDT.table;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.jdbdt.TestUtil.expectAssertionError;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DBTableExistenceAssertionsTest extends DBTestCase {

  private static Table table;

  private static final String NON_EXISTING_TABLE = "FooTable";
  private static final String EMPTY_MSG = "";
  private static final String EXISTS_MSG = "exists";
  private static final String NOT_EXISTS_MSG = "does not exist";
  
  @BeforeClass
  public static void globalSetup() {
    table = table(UserDAO.TABLE_NAME)
           .columns(UserDAO.COLUMNS)
           .build(getDB());
  }

  @Test 
  public void testTableExists1() {
    assertTableExists(getDB(), table.getName());
  }
  
  @Test 
  public void testTableExists2() {
    assertTableExists(EXISTS_MSG, getDB(), table.getName());
  }
  
  @Test
  public void testTableExists3() throws SQLException {
    expectAssertionError(EMPTY_MSG, 
                         () -> assertTableExists(getDB(), NON_EXISTING_TABLE));
  }
  
  @Test
  public void testTableExists4() throws SQLException {
    expectAssertionError(EXISTS_MSG, 
                         () -> assertTableExists(EXISTS_MSG, getDB(), NON_EXISTING_TABLE));
  }
  
  
  
  @Test 
  public void testTableDoesNotExist1() {
    assertTableDoesNotExist(getDB(), NON_EXISTING_TABLE);
  }
  
  @Test 
  public void testTableDoesNotExist2() {
    assertTableDoesNotExist(NOT_EXISTS_MSG, getDB(), NON_EXISTING_TABLE);
  }
  
  @Test
  public void testTableDoesNotExist3() throws SQLException {
    expectAssertionError(EMPTY_MSG, 
                         () -> assertTableDoesNotExist(getDB(), table.getName()));
  }
  
  @Test
  public void testTableDoesNotExist4() throws SQLException {
    expectAssertionError(NOT_EXISTS_MSG, 
                         () -> assertTableDoesNotExist(NOT_EXISTS_MSG, getDB(), table.getName()));
  }
  
  @Test
  public void testTableExistsAfterDrop() throws Throwable {
    getDAO().dropTable();
    try {
      expectAssertionError(EMPTY_MSG, 
        () -> assertTableExists(getDB(), table.getName()));
    }
    finally {
      getDAO().createTable();
    }
  }
  
  @Test
  public void testTableDoesNotExistAfterDrop() throws Throwable {
    getDAO().dropTable();
    try {
      assertTableDoesNotExist(getDB(), table.getName());
    }
    finally {
      getDAO().createTable();
    }
  }

//  @Test 
//  public void testTableExists33() {
//    assertTableExists(getDB(), table.getName());
//  }
//  
//  @Test 
//  public void testTableExists44() {
//    assertTableExists("exists", getDB(), table.getName());
//  }
//  
//  @Test
//  public void testTableDoesNotExist1() throws SQLException {
//    expectAssertionError("", () -> assertTableDoesNotExist(getDB(), table.getName()));
//  }
//  
//  @Test
//  public void testTableDoesNotExist2() throws SQLException {
//    expectAssertionError("does not exist", () -> assertTableDoesNotExist("not", getDB(), table.getName()));
//  }
//  
//  

//  
//  @Test 
//  public void testTableDoesNotExist3() throws SQLException {
//    getDAO().dropTable();
//    try {
//      assertTableDoesNotExist(getDB(), table.getName());
//    }
//    finally {
//      getDAO().createTable();
//    }   
//  }
  
 
}
