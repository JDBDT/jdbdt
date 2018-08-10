package org.jdbdt;

import static org.junit.Assert.*;
import static org.jdbdt.JDBDT.*;
import static org.jdbdt.TestUtil.*;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TableTest extends DBTestCase {  
  private Table theSUT;
  
  @Before
  public void createTable() {
    theSUT = table(UserDAO.TABLE_NAME).columns(UserDAO.COLUMNS).build(getDB());
  }
  
  @Test
  public void testInit() {
    assertEquals(UserDAO.TABLE_NAME, theSUT.getName());
  }
  
  @Test
  public void testQueryExecution() {
    DataSet actual = executeQuery(theSUT);
    DataSet expected = 
      data(theSUT, getConversion())
        .rows(INITIAL_DATA);
    assertDataSet(expected, actual);
  }
  
  @Test
  public void testXXX() {
    theSUT = table(UserDAO.TABLE_NAME).columns("XXX").build(getDB());
  }
}
