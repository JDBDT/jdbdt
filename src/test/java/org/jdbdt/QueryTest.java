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


import static org.jdbdt.JDBDT.*;
import static org.junit.Assert.*;
import static org.jdbdt.TestUtil.*;

import java.sql.SQLException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryTest extends DBTestCase { 
  
  private final static String SQL_CODE = 
      "SELECT login, password FROM Users WHERE login LIKE ?";
 
  @Test
  public void testInit() {
    Object[] qargs = { "x" };
    DataSource theSUT = query(getDB(), SQL_CODE, qargs);
    assertEquals(SQL_CODE, theSUT.getSQLForQuery());
    assertArrayEquals(qargs, theSUT.getQueryArguments());
  }
  
  @Test
  public void testInit2() {
    DataSource theSUT = query(getDB(), SQL_CODE);
    assertEquals(SQL_CODE, theSUT.getSQLForQuery());
    assertArrayEquals(new Object[]{}, theSUT.getQueryArguments());
  }
  
  @Test
  public void testQueryExecution1() {
    DataSource theSUT = query(getDB(), SQL_CODE, "%");
    DataSet actual = executeQuery(theSUT);
    DataSet expected = data(theSUT);
    for (User u : INITIAL_DATA) {
      expected.row(u.getLogin(), u.getPassword());
    }
    assertDataSet(expected, actual);
  }
  
  @Test
  public void testQueryExecution2() throws SQLException {
    DataSource theSUT = query(getDB(), SQL_CODE, EXISTING_DATA_ID1);
    User u = getDAO().query(EXISTING_DATA_ID1);
    DataSet actual = executeQuery(theSUT);
    DataSet expected = 
      data(theSUT).row(u.getLogin(), u.getPassword());
    assertDataSet(expected, actual);
  }

}
