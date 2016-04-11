package org.jdbdt;


import static org.jdbdt.JDBDT.*;
import static org.junit.Assert.*;
import static org.jdbdt.TestUtil.*;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SQLDataSourceTest extends DBTestCase { 


  private SQLDataSource theSUT;
  private final String SQL_CODE = 
      "SELECT login, password FROM Users WHERE login LIKE ?";
 
  @Before
  public void createDataSource() throws SQLException {
    theSUT = getDB().source(SQL_CODE);
  }

  @Test
  public void testInit() {
    assertEquals(SQL_CODE, theSUT.getSQLForQuery());
  }
  
  @Test
  public void testInitQueryArguments() {
    Object[] args = { "foo" };
    SQLDataSource r = theSUT.withArguments(args);
    assertSame(theSUT, r);
    assertArrayEquals(args, theSUT.getQueryArguments());
  }
 
  @Test 
  public void testInitArgumentsTwice() {
    theSUT.withArguments("foo");
    expectException(InvalidOperationException.class, 
                    () -> theSUT.withArguments("foo2"));
  }
 
  @Test
  public void testInitArgumentsAfterCompiling() {
    theSUT.getQueryStatement();
    expectException(InvalidOperationException.class, 
        () -> theSUT.withArguments("foo"));
  }
  
  void matchDataSets(DataSet expected, DataSet actual) {
    actual.normalizeRowOrder();
    expected.normalizeRowOrder();
    assertTrue(expected.sameDataAs(actual));
  }
  
  @Test
  public void testQueryExecution1() {
    DataSet actual = 
      theSUT.withArguments("%").executeQuery(false);
    DataSet expected = data(theSUT);
    for (User u : INITIAL_DATA) {
      expected.row(u.getLogin(), u.getPassword());
    }
    matchDataSets(actual, expected);
  }
  
  @Test
  public void testQueryExecution2() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    DataSet actual = 
      theSUT.withArguments(EXISTING_DATA_ID1)
            .executeQuery(false);
    DataSet expected = 
      data(theSUT)
       .row(u.getLogin(), u.getPassword());
    matchDataSets(actual, expected);
  }

}
