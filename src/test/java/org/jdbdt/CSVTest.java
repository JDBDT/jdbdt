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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.jdbdt.CSV.LineSeparator;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(TestCategories.CSVSupport.class)
public class CSVTest extends DBTestCase {

  private static Table table;
  private static DataSet initialDataSet;
  private static int testCounter;
  
  @BeforeClass
  public static void globalSetup() {
    table = table(UserDAO.TABLE_NAME)
           .columns(UserDAO.COLUMNS)
           .key(UserDAO.PRIMARY_KEY)
           .build(getDB());
    initialDataSet = data(table, getConversion()).rows(INITIAL_DATA);
    testCounter = 0;
  }
  
  void performTest(CSV.Format format) throws IOException, SQLException {
    File f = new File("data_" + DBTestCase.gTestSuiteCounter  + "-" + testCounter + ".csv");
    testCounter++;
    CSV.write(initialDataSet, format, f);
    getDAO().doDeleteAll();
    DataSet readDataSet = CSV.read(table, format, f); 
    insert(readDataSet);
    DataSet dataSetInDB = executeQuery(table); 
    assertEquals(initialDataSet, dataSetInDB);
  }
  
  @Test
  public void test1() throws IOException, SQLException {
    performTest(new CSV.Format());
  }
  
  @Test
  public void test2() throws IOException, SQLException {
    performTest(new CSV.Format().hasHeader());
  }
  
  @Test
  public void test3() throws IOException, SQLException {
    performTest(new CSV.Format().separator("\t"));
  }
  
  @Test
  public void test4() throws IOException, SQLException {
    performTest(new CSV.Format().separator("\t").hasHeader());
  }
  
  @Test
  public void test5() throws IOException, SQLException {
    performTest(new CSV.Format().separator("\t").hasHeader().nullValue("NULL"));
  }
  
  @Test
  public void test6() throws IOException, SQLException {
    performTest(new CSV.Format().separator(";").nullValue("NULL"));
  }
  
  @Test
  public void test7() throws IOException, SQLException {
    performTest(new CSV.Format().separator("\t").nullValue("NULL").lineComment("---"));
  }
  
  @Test
  public void test8() throws IOException, SQLException {
    performTest(new CSV.Format().lineSeparator(LineSeparator.CR));
  }
  
  
  @Test
  public void test9() throws IOException, SQLException {
    performTest(new CSV.Format().lineSeparator(LineSeparator.LF));
  }
  
  @Test
  public void test10() throws IOException, SQLException {
    performTest(new CSV.Format().lineSeparator(LineSeparator.CR_LF));
  }

}
