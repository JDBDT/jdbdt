/*
 * The MIT License
 *
 * Copyright (c) Eduardo R. B. Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
