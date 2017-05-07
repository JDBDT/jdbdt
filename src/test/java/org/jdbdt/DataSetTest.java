package org.jdbdt;

import static org.jdbdt.JDBDT.*;
import static org.jdbdt.TestUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataSetTest extends DBTestCase {

  private static Table table;
 
  @BeforeClass
  public static void globalSetup() {
    table = table(UserDAO.TABLE_NAME)
           .columns(UserDAO.COLUMNS)
           .build(getDB());
  }

  private DataSet theSUT;

  private Object[][] genData(int n) {
    Object[][] r = new Object[n][];
    for (int i = 0; i < n; i++) {
      r[i] = rowFor(buildNewUser());
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
    Object[] data = rowFor(buildNewUser());
    expectException(InvalidOperationException.class,
        () -> theSUT.row(data));
  }
  
  @Test
  public void testSetReadOnly3() {
    theSUT.setReadOnly();
    Object[][] data = { 
      rowFor(buildNewUser()),
      rowFor(buildNewUser())
    };
    expectException(InvalidOperationException.class,
        () -> theSUT.rows(data));
  }
  
  @Test
  public void testSetReadOnly4() {
    theSUT.setReadOnly();
    
    expectException(InvalidOperationException.class,
        () -> theSUT.add(data(table).rows(genData(1))));
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
  
  @Test(expected=InvalidOperationException.class)
  public void testInvalidRow1() {
    theSUT.row("Not enough fields");
  }
  
  @Test
  public void testChaining1() {
    assertSame(theSUT, theSUT.row(rowFor(buildNewUser())));
  }
  
  @Test
  public void testChaining2() {
    assertSame(theSUT, theSUT.rows(genData(1)));
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
    DataSet res = theSUT.add(other);
    assertSame(theSUT, res);
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
    DataSet res = theSUT.add(other);
    assertSame(theSUT, res);
    assertEquals(r1.length + r2.length, theSUT.size());
    assertEquals(expected, theSUT.getRows());
  }
  
  @Test
  public void testAdd3() {
    Object[][] r = genData(2);
    theSUT.rows(r);
    DataSet other = data(table);
    DataSet res = theSUT.add(other);
    assertSame(theSUT, res);
    assertEquals(r.length, theSUT.size());
    assertEquals(lRow(r), theSUT.getRows());
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testAdd4() {
    DataSource q = select("LOGIN").from(table).build(getDB());
    theSUT.add( data(q).row("foo") );
  }
  
  
  private void testSubsetMethod
  (int size, int start, int n, Supplier<DataSet> action) {
    Object[][] r1 = genData(size);
    Object[][] r2 = subData(r1, start, n);
    theSUT.rows(r1);
    DataSet res = action.get();
    assertEquals(lRow(r2), res.getRows());
    assertEquals(lRow(r1), theSUT.getRows());
    assertSame(table, res.getSource());
  }
  
  @Test
  public void testSubset0() {
    final int size = 10, start = 1, n = 0;
    testSubsetMethod(size, start, n, 
        () -> DataSet.subset(theSUT, start, n));
  }
  
  @Test
  public void testSubset1() {
    final int size = 10, start = 1, n = 5;
    testSubsetMethod(size, start, n, 
        () -> DataSet.subset(theSUT, start, n));
  }
  
  @Test
  public void testSubset2() {
    final int size = 10, start = 0, n = size;
    testSubsetMethod(size, start, n, 
        () -> DataSet.subset(theSUT, start, n));
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testSubsetInvArg1() {
    DataSet.subset(theSUT, -1, 1);
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testSubsetInvArgs2() {
    DataSet.subset(theSUT, 0, -1);
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testSubsetInvArgs3() {
    theSUT.rows(genData(5));
    DataSet.subset(theSUT, 1, 5);
  }
  
  @Test(expected=InvalidOperationException.class)
  public void testSubsetInvArgs4() {
    theSUT.rows(genData(5));
    DataSet.subset(theSUT, 0, 6);
  }
  
  @Test
  public void testHead1() {
    final int size = 10, n = 1;
    testSubsetMethod(size, 0, n, 
        () -> DataSet.first(theSUT, n));
  }
  
  @Test
  public void testHead2() {
    final int size = 10, n = 9;
    testSubsetMethod(size, 0, n, 
        () -> DataSet.first(theSUT, n));
  }
  
  @Test
  public void testTail1() {
    final int size = 10, n = 1;
    testSubsetMethod(size, size - n, n, 
        () -> DataSet.last(theSUT, n));
  }
  
  @Test
  public void testTail2() {
    final int size = 10, n = 9;
    testSubsetMethod(size, size - n, n, 
        () -> DataSet.last(theSUT, n));
  }
  
  @Test
  public void testSingleton() {
    final int size = 10, index = 5;
    testSubsetMethod(size, index, 1, 
        () -> DataSet.singleton(theSUT, index));
  }
  
  @Test 
  public void testBuild() {
    assertSame(theSUT, theSUT.build().data());
  }
  
  @Test
  public void testSameDataAs() {
    Object[][] r = genData(5);
    theSUT.rows(r);
    DataSet other = data(table).rows(r);
    assertTrue(theSUT.sameDataAs(other));
  }
  
  @Test
  public void testNormalizeRowOrder() {
    Object[][] r = genData(50);
    Collections.shuffle(Arrays.asList(r));
    theSUT.rows(r).normalizeRowOrder();
    Arrays.sort(r, (a,b) 
        -> Integer.compare(
            new Row(a).hashCode(), 
            new Row(b).hashCode()));
    assertEquals(lRow(r), theSUT.getRows());
  }
  
}
