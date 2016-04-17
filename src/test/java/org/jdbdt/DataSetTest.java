package org.jdbdt;

import static org.jdbdt.JDBDT.*;
import static org.jdbdt.TestUtil.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    assertEquals(0, theSUT.getRows().size());
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
  
  private Object[][] genData(int n) {
    Object[][] r = new Object[n][];
    for (int i = 0; i < n; i++) {
      r[i] = rowFor(createNewUser());
    }
    return r;
  }
  
  private Object[][] subData(Object[][] d, int start, int n) {
    Object[][] r = new Object[n][];
    for (int i = 0; i < n; i++) {
      r[i] = d[start + i];
    }
    return r;
  }
  
  private List<Row> lRow(Object[][]... rSets) {
    ArrayList<Row> list = new ArrayList<>();
    for (Object[][] rSet : rSets) {
      for (Object[] r : rSet) {
        list.add(new Row(r));
      }
    }
    return list;
  }

  private void testRowAddition(int n, boolean allAtOnce) {
    Object[][] data = genData(n);
    if (allAtOnce) {
      theSUT.rows(data);
    } else {
      for (Object[] r : data) {
        theSUT.row(r);
      }
    }
    assertFalse(theSUT.isEmpty());
    assertEquals(n, theSUT.size());
    assertEquals(lRow(data), theSUT.getRows());
  }
  
  @Test
  public void testRow1() {
    testRowAddition(1, false);
  }
  
  @Test
  public void testRow5() {
    testRowAddition(5, false);
  }
  
  @Test
  public void testRow500() {
    testRowAddition(500, false);
  }
  
  @Test
  public void testRows1() {
    testRowAddition(1, true);
  }
  
  @Test
  public void testRows5() {
    testRowAddition(5, true);
  }
  
  @Test
  public void testRows500() {
    testRowAddition(500, true);
  }
  
  @Test
  public void testAdd1() {
    Object[][] data = genData(5);
    DataSet other = data(table).rows(data);
    theSUT.add(other);
    assertEquals(data.length, theSUT.size());
    assertEquals(lRow(data), theSUT.getRows());
  }
  
  @Test
  public void testAdd2() {
    Object[][] r1 = genData(2);
    Object[][] r2 = genData(5);
    List<Row> expected = lRow(r1, r2);
    theSUT.rows(r1);
    DataSet other = data(table).rows(r2);
    theSUT.add(other);
    assertEquals(expected, theSUT.getRows());
  }
  
  @Test
  public void testAdd3() {
    Object[][] r = genData(2);
    theSUT.rows(r);
    DataSet other = data(table);
    theSUT.add(other);
    assertEquals(lRow(r), theSUT.getRows());
  }
  
  @Test
  public void testSubset() {
    final int size = 10;
    final int start = 1;
    final int n = 5;
    Object[][] r1 = genData(size);
    Object[][] r2 = subData(r1, start, n);
    theSUT.rows(r1);
    DataSet s = DataSet.subset(theSUT, start, n);
    assertEquals(lRow(r2), s.getRows());
  }
  
  @Test
  public void testHead1() {
    final int size = 10;
    final int n = 1;
    Object[][] r1 = genData(size);
    Object[][] r2 = subData(r1, 0, n);
    theSUT.rows(r1);
    DataSet s = DataSet.head(theSUT, n);
    assertEquals(lRow(r2), s.getRows());
  }
  
  @Test
  public void testHead2() {
    final int size = 10;
    final int n = 9;
    Object[][] r1 = genData(size);
    Object[][] r2 = subData(r1, 0, n);
    theSUT.rows(r1);
    DataSet s = DataSet.head(theSUT, n);
    assertEquals(lRow(r2), s.getRows());
  }
  
  @Test
  public void testTail1() {
    final int size = 10;
    final int n = 1;
    Object[][] r1 = genData(size);
    Object[][] r2 = subData(r1, size - n - 1, n);
    theSUT.rows(r1);
    DataSet s = DataSet.tail(theSUT, n);
    assertEquals(lRow(r2), s.getRows());
  }
  
  @Test
  public void testTail2() {
    final int size = 10;
    final int n = 9;
    Object[][] r1 = genData(size);
    Object[][] r2 = subData(r1, size - n - 1, n);
    theSUT.rows(r1);
    DataSet s = DataSet.tail(theSUT, n);
    assertEquals(lRow(r2), s.getRows());
  }
 
}
