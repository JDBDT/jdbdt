package org.jdbdt;

import static org.jdbdt.JDBDT.*;
import static org.jdbdt.TestUtil.*;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataSetTest extends DBTestCase {

  @Rule public TestName testName = new TestName();

  private static Table table;
 
  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = getDB().table(UserDAO.TABLE_NAME)
                   .columns(UserDAO.COLUMNS);
  }

  private DataSet theSUT;

  @Before 
  public void createDataSet() {
    theSUT = data(table);
  }
  
  
  @Test
  public void testInit() {
    assertFalse(theSUT.isReadOnly());
    assertTrue(theSUT.isEmpty());
    assertEquals(0, theSUT.size());
    assertSame(table, theSUT.getSource());
  }
  
  @Test
  public void testSetReadOnly1() {
    theSUT.setReadOnly();
    assertTrue(theSUT.isReadOnly());
  }
  
  @Test
  public void testSetReadOnly2() {
    theSUT.setReadOnly();
    Object[] data = rowFor(createNewUser());
    expectException(InvalidOperationException.class,
        () -> theSUT.row(data));
  }
  
  @Test
  public void testSetReadOnly3() {
    theSUT.setReadOnly();
    Object[][] data = { 
      rowFor(createNewUser()),
      rowFor(createNewUser())
    };
    expectException(InvalidOperationException.class,
        () -> theSUT.rows(data));
  }

  private void testRowAddition(int n) {
    ArrayList<Row> expected = new ArrayList<>();
    for (int i=0; i < n; i++) {
      Object[] data = rowFor(createNewUser());
      theSUT.row(data);
      expected.add(new Row(data));
    }
    assertFalse(theSUT.isEmpty());
    assertEquals(n, theSUT.size());
    assertEquals(expected, theSUT.getRows());
  }
  
  @Test
  public void testRowAddition1() {
    testRowAddition(1);
  }
  
  @Test
  public void testRowAddition5() {
    testRowAddition(5);
  }
  
  @Test
  public void testRowAddition5000() {
    testRowAddition(5000);
  }
  
}
