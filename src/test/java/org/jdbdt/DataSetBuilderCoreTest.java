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

import static org.junit.Assert.*;
import static org.jdbdt.JDBDT.*;
import static org.jdbdt.TestUtil.*;

import java.sql.SQLException;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataSetBuilderCoreTest extends DBTestCase {

  private static Table table;

  private static final ColumnFiller<?> DUMMY_FILLER
  = new ColumnFiller<Object>() {
    @Override
    public Object next() {
      return null;
    }
  };
  
  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = table(UserDAO.TABLE_NAME).columns(UserDAO.COLUMNS).build(getDB());
  }

  DataSetBuilder theSUT;

  @Before 
  public void setUp() {
    theSUT = builder(table);
  }

  private static void assertEmptyDataSet(DataSetBuilder sut) {
    assertEquals("filler count", 0, sut.fillerCount());
    assertEquals("empty row list", 0, sut.data().size());
  }

  @Test
  public void testConstruction() {
    assertEmptyDataSet(theSUT);
  }
  
  private static void 
  invalidUse
  (DataSetBuilder sut, Consumer<DataSetBuilder> operation, Consumer<DataSetBuilder> assertions) {
    expectException(InvalidOperationException.class, () -> operation.accept(sut));
    assertions.accept(sut);
  }

  private void invalidFiller(String c, ColumnFiller<?> f) {
    invalidUse(theSUT,
               sut -> sut.set(c,f),
               DataSetBuilderCoreTest::assertEmptyDataSet
               );
  }
  @Test
  public void testInvalidFiller1() {
    invalidFiller("InvalidField",  DUMMY_FILLER);
  }
  @Test
  public void testInvalidFiller2() {
    invalidFiller("login",  null);
  }
  @Test
  public void testInvalidFiller3() {
    invalidFiller(null,  DUMMY_FILLER);
  }
  @Test
  public void testValidFiller() {
    theSUT.value("login", "root");
    assertEquals("fillers set", 1, theSUT.fillerCount());
  }
  @Test
  public void testFillerReset() {
    theSUT.value("login", "root");
    theSUT.reset();
    assertEquals("fillers set", 0, theSUT.fillerCount());
  }
  @Test
  public void testAllFillersSet() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    Object[] row = getConversion().convert(u);
    for (int c = 0; c < row.length; c++) {
      theSUT.value(UserDAO.COLUMNS[c], row[c]);
    }
    assertEquals("fillers set", UserDAO.COLUMNS.length, theSUT.fillerCount());
    assertEquals("no rows", 0, theSUT.data().size());
  }
  private void checkMissingFillers(int N) {
    invalidUse
    (theSUT,
     sut -> sut.generate(1),
     sut -> {
       assertEquals("fillers set", N, sut.fillerCount());
       assertEquals("no rows", 0, sut.data().size());
     });
  }
  @Test
  public void testMissingFillers0() throws SQLException {
    checkMissingFillers(0);
  }
  @Test
  public void testMissingFillers1() throws SQLException {
    theSUT.value("login", "root");
    checkMissingFillers(1);
  }
  @Test
  public void testMissingFillersAllButOne() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    Object[] row = getConversion().convert(u);
    for (int c = 0; c < row.length-1; c++) {
      theSUT.value(UserDAO.COLUMNS[c], row[c]);
    }
    checkMissingFillers(row.length-1);
  }
  @Test
  public void testGenerate0() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    Object[] row = getConversion().convert(u);
    for (int c = 0; c < row.length; c++) {
      theSUT.value(UserDAO.COLUMNS[c], row[c]);
    }
    invalidUse
    (theSUT,
     sut -> sut.generate(0),
     sut -> {
       assertEquals("fillers set", UserDAO.COLUMNS.length, sut.fillerCount());
       assertEquals("no rows", 0, sut.data().size());
    });
  }
  
  @Test
  public void testGenerateMinus1() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    Object[] row = getConversion().convert(u);
    for (int c = 0; c < row.length; c++) {
      theSUT.value(UserDAO.COLUMNS[c], row[c]);
    }
    invalidUse
    (theSUT,
     sut -> sut.generate(-1),
     sut -> {
       assertEquals("fillers set", UserDAO.COLUMNS.length, sut.fillerCount());
       assertEquals("no rows", 0, sut.data().size());
    });
  }
  
  private void testSimpleGeneration(final int N) throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    Object[] rowData = getConversion().convert(u);
    DataSet expectedRows = new DataSet(table);
    for (int i=0; i < N; i++) {
      expectedRows.addRow(new Row(rowData));
    }
    for (int col = 0; col < rowData.length; col++) {
      theSUT.value(UserDAO.COLUMNS[col], rowData[col]);
    }
    theSUT.generate(N);
    assertEquals("fillers set", UserDAO.COLUMNS.length, theSUT.fillerCount());
    assertTrue(expectedRows.sameDataAs(theSUT.data()));
  }
  @Test
  public void testGenerateOneRow() throws SQLException {
    testSimpleGeneration(1);
  }

  @Test
  public void testGenerate100Rows() throws SQLException {
    testSimpleGeneration(100);
  }
 
}
