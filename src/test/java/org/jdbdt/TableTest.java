package org.jdbdt;


import static org.jdbdt.JDBDT.table;

import java.sql.SQLException;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TableTest extends DBTestCase {  
  private Table theSUT;
  
  @Before
  public void setup() throws SQLException {
    theSUT = table(UserDAO.TABLE_NAME);
  }
  
  @Test
  public void testInit() {
    assertEquals(UserDAO.TABLE_NAME, theSUT.getName());
    assertEquals(-1, theSUT.getColumnCount());
    assertNull(theSUT.getColumnNames());
  }
  
  @Test
  public void testInitColumns() {
    theSUT.columns(UserDAO.COLUMNS);
    assertEquals(UserDAO.COLUMNS.length, theSUT.getColumnCount());
    assertArrayEquals(UserDAO.COLUMNS,theSUT.getColumnNames());
  }
  
  @Test(expected=InvalidUsageException.class)
  public void testReInitColumns() {
    try {
      theSUT.columns(UserDAO.COLUMNS);
    } catch(Throwable e) {
      fail("Unexpected exception");
    }
    theSUT.columns(UserDAO.COLUMNS);
  }
  
  @Test(expected=InvalidUsageException.class)
  public void testWhenNotBound() {
    theSUT.getConnection();
  }
  
  @Test
  public void testBinding() throws SQLException {
    theSUT.boundTo(getConnection());
    assertSame(theSUT.getConnection(), getConnection());
  }
  
  @Test(expected=InvalidUsageException.class)
  public void testRebinding() throws SQLException {
    try {
      theSUT.boundTo(getConnection());
    }
    catch(Throwable e) {
      fail("Unexpected error");
    }
    theSUT.boundTo(getConnection());
  }
  
}
