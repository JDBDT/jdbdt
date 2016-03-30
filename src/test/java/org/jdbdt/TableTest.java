package org.jdbdt;


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
    theSUT = getDB().table(UserDAO.TABLE_NAME);
  }
  
  @Test
  public void testInit() {
    assertEquals(UserDAO.TABLE_NAME, theSUT.getName());
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
  
}
