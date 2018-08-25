package org.jdbdt;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RowTest {

  static final Object DATA[] = { null, 1, "2", 3.0, new byte[]{ 4, 5, 6} };
  
  Row theSUT;
  
  @Before
  public void setup() {
    theSUT = new Row(DATA);
  }
  
  @Test
  public void testInit() {
    assertSame(DATA, theSUT.data());
    assertEquals(DATA.length, theSUT.length());
  }
  
  @Test
  public void testHashCode() {
    int expected = Arrays.deepHashCode(DATA);
    assertEquals(expected, theSUT.hashCode());
  }
  
  @Test
  public void testToString() {
    String expected = Arrays.deepToString(DATA);
    assertEquals(expected, theSUT.toString());
  }
  
  @Test
  public void testEquals1() {
    assertTrue(theSUT.equals(theSUT));
  }
  
  @Test
  public void testEquals2() {
    assertTrue(theSUT.equals(new Row(DATA)));
  }
  
  @Test
  public void testEquals3() {
    assertTrue(theSUT.equals(new Row(DATA.clone())));
  }
  
  @Test
  public void testEquals4() {
    Object[] d = DATA.clone();
    d[2] = new int[] { 1, 2, 3};
    assertFalse(theSUT.equals(new Row(d)));
  }
  
  @Test
  public void testEquals5() {
    Object[] d = DATA.clone();
    d[2] = new int[] { 1, 2, 3};
    assertFalse(theSUT.equals(new Row(d)));
  }
  
  @Test
  public void testEquals6() {
    assertFalse(theSUT.equals(null));
  }
  
  @Test
  public void testEquals7() {
    assertFalse(theSUT.equals("X"));
  }
}
