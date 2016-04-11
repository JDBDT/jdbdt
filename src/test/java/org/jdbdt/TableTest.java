package org.jdbdt;


import java.sql.SQLException;

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
  public void createTable() throws SQLException {
    theSUT = getDB().table(UserDAO.TABLE_NAME);
  }
  
  @Test
  public void testInit() {
    assertEquals(UserDAO.TABLE_NAME, theSUT.getName());
  }
  
  @Test
  public void testReInitColumns() {
    theSUT.columns(UserDAO.COLUMNS);
    expectException(InvalidOperationException.class,
     () -> theSUT.columns(UserDAO.COLUMNS));
  }
  
  @Test
  public void testQueryExecution() {
    DataSet actual = theSUT.executeQuery(false);
    DataSet expected = 
      data(theSUT, getConversion())
        .rows(INITIAL_DATA);
    assertDataSet(expected, actual);
  }
  
}
