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

import static org.junit.Assert.*;
import static org.jdbdt.JDBDT.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataSetBuilderCoreFillerTest extends DBTestCase {


  private static Table table;

  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = table(UserDAO.TABLE_NAME).columns(UserDAO.COLUMNS).build(getDB());
  }

  DataSetBuilder theSUT;

  static final 
  User BASE_DATA = new User("someLogin", 
                            "someName", 
                            "somePassword",
                            Date.valueOf("2015-12-31"));
  @Before 
  public void setUp() {
    theSUT = builder(table);
    //"login", "name", "password", "created" 
    theSUT.value("login", BASE_DATA.getLogin())
          .value("name", BASE_DATA.getName())
          .value("password", BASE_DATA.getPassword())
          .value("created",  dateValue(BASE_DATA.getCreated())); 
  }
  
  interface UserGenerator {
    User next(User base, int index);
  }
  private static void gen(DataSet rs, UserGenerator g, int n) {
    for (int i=0; i < n; i++) {
      User data = g.next(BASE_DATA, i);
      rs.addRow(new Row(getConversion().convert(data)));
    }
  }
  private static DataSet deriveRowSet(int n, UserGenerator ug) {
    DataSet rs = new DataSet(table);
    gen(rs, ug, n);
    return rs;
  }
  @Test
  public void testSeq1() {
    String userPrefix = "someLogin";
    int count = 10;
    DataSet expected = deriveRowSet(count, (u,i) -> {
      User r = u.clone();
      r.setLogin(userPrefix + i);
      return r;
    });
    theSUT.sequence("login", i -> userPrefix + i);
    theSUT.generate(count);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
  
  @Test
  public void testSeq2() {
    String userPrefix = "someLogin";
    char suffix = '_';
    int count = 10;
    DataSet expected = deriveRowSet(count, (u,i) -> {
      StringBuilder sb = new StringBuilder();
      for (int k = 0; k < i; k++) {
        sb.append(suffix);
      }
      User r = u.clone();
      r.setLogin(userPrefix + sb.toString());
      return r;
    });
    theSUT.sequence("login", userPrefix, s -> s + "_" );
    theSUT.generate(count);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
  @Test
  public void testSeq3() {
    String userPrefix = "someLogin";
    int count = 10;
    DataSet expected = deriveRowSet(count, (u,i) -> {
      User r = u.clone();
      r.setLogin(userPrefix + i);
      return r;
    });
    String[] values = new String[count];
    for (int i=0; i < count; i++) {
      values[i] = userPrefix + i;
    }
    theSUT.sequence("login", values);
    theSUT.generate(count);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
  @Test
  public void testSeq4() {
    String userPrefix = "someLogin";
    int count = 10;
    DataSet expected = deriveRowSet(count, (u,i) -> {
      User r = u.clone();
      r.setLogin(userPrefix + i);
      return r;
    });
    String[] values = new String[count];
    for (int i=0; i < count; i++) {
      values[i] = userPrefix + i;
    }
    theSUT.sequence("login", Arrays.asList(values));
    theSUT.generate(count);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
  
  @Test(expected=ColumnFillerException.class)
  public void testColumnFillerException() {
    theSUT.set("login", () -> { throw new RuntimeException(); });
    theSUT.generate(1);
  }
 
  @Test
  public void testNullValue() {
    int count = 10;
    DataSet expected = deriveRowSet(2 * count, (u,i) -> {
      User r = u.clone();
      if (i >= 10) {
        r.setLogin(null);
      }
      return r;
    });
    theSUT.generate(count);
    theSUT.nullValue("login");
    theSUT.generate(count);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
  @Test
  public void testRemainingColumnsNull() {
    int count = 10;
    DataSet expected = deriveRowSet(count, (u,i) -> {
      User r = u.clone();
      r.setName(null);
      r.setPassword(null);
      r.setCreated(null);
      return r;
    });
    theSUT.reset();
    theSUT.value("login", BASE_DATA.getLogin());
    theSUT.remainingColumnsNull();
    theSUT.generate(count);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }

  @Test
  public void testAllColumnsNull() {
    int count = 10;
    DataSet expected = deriveRowSet(count, (u,i) -> {
      User r = u.clone();
      r.setLogin(null);
      r.setName(null);
      r.setPassword(null);
      r.setCreated(null);
      return r;
    });
    theSUT.reset();
    theSUT.value("login", BASE_DATA.getLogin());
    theSUT.allColumnsNull();
    theSUT.generate(count);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
}
