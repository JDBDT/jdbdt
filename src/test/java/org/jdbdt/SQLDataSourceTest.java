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
public class SQLDataSourceTest extends DBTestCase { 
  
  private final String SQL_CODE = 
      "SELECT login, password FROM Users WHERE login LIKE ?";
 
  @Test
  public void testInit() {
    Object[] qargs = { "x" };
    SQLDataSource theSUT = new SQLDataSource(getDB(), SQL_CODE, qargs);
    assertEquals(SQL_CODE, theSUT.getSQLForQuery());
    assertArrayEquals(qargs, theSUT.getQueryArguments());
  }
  
  @Test
  public void testInit2() {
    SQLDataSource theSUT = new SQLDataSource(getDB(), SQL_CODE);
    assertEquals(SQL_CODE, theSUT.getSQLForQuery());
    assertNull(theSUT.getQueryArguments());
  }
  
  @Test
  public void testQueryExecution1() {
    SQLDataSource theSUT = new SQLDataSource(getDB(), SQL_CODE, "%");
    DataSet actual = query(theSUT);
    DataSet expected = data(theSUT);
    for (User u : INITIAL_DATA) {
      expected.row(u.getLogin(), u.getPassword());
    }
    assertDataSet(expected, actual);
  }
  
  @Test
  public void testQueryExecution2() throws SQLException {
    SQLDataSource theSUT = new SQLDataSource(getDB(), SQL_CODE, EXISTING_DATA_ID1);
    User u = getDAO().query(EXISTING_DATA_ID1);
    DataSet actual = query(theSUT);
    DataSet expected = 
      data(theSUT).row(u.getLogin(), u.getPassword());
    assertDataSet(expected, actual);
  }

}
