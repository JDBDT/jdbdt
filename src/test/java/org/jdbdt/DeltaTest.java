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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeltaTest {

  Iterator<Row> empty() {
    return Collections.emptyIterator();
  }
  
  Delta emptyDelta() {
    return new Delta(empty(), empty());
  }
  
  Iterator<Row> rows(Object... v) {
    ArrayList<Row> list = new ArrayList<>();
    for (Object o : v) {
      list.add(new Row(new Object[] { o }));
    }
    return list.iterator();
  }
  
  void assertIteration(Iterator<Row> exp, Iterator<Row> actual) {
    while (exp.hasNext()) {
      assertTrue(actual.hasNext());
      assertEquals(exp.next(), actual.next()); 
    }
    assertFalse(actual.hasNext());
  }
 
  @Test
  public void testEmptyDelta1() {
    Delta d = emptyDelta();
    assertTrue(d.isEmpty());
    assertFalse(d.deleted().hasNext());
    assertFalse(d.inserted().hasNext());
  }
  
  @Test(expected=NoSuchElementException.class)
  public void testEmptyDelta2() {
    emptyDelta().deleted().next();
  }
  
  @Test(expected=NoSuchElementException.class)
  public void testEmptyDelta3() {
    emptyDelta().inserted().next();
  }
  
  @Test 
  public void testEmptyDelta4() {
    Delta d = new Delta(rows(1), rows(1));
    assertTrue(d.isEmpty());
  }

  @Test 
  public void testEmptyDelta5() {
    Delta d = new Delta(rows(1,2), rows(2,1));
    assertTrue(d.isEmpty());
  }
  
  @Test 
  public void testEmptyDelta6() {
    Delta d = new Delta(rows(1,1,1,1,2,2), rows(2,1,2,1,1,1));
    assertTrue(d.isEmpty());
  }
  
  @Test 
  public void testDiff1() {
    Delta d = new Delta(rows(1), rows(2));
    assertIteration(rows(1), d.deleted());
    assertIteration(rows(2), d.inserted());
  }
  
  @Test 
  public void testDiff2() {
    Delta d = new Delta(rows(1), empty());
    assertIteration(rows(1), d.deleted());
    assertIteration(empty(), d.inserted());
  }
  
  @Test 
  public void testDiff3() {
    Delta d = new Delta(empty(), rows(1));
    assertIteration(empty(), d.deleted());
    assertIteration(rows(1), d.inserted());
  }
  
  @Test 
  public void testDiff4() {
    Delta d = new Delta(empty(), rows(1,1));
    assertIteration(empty(), d.deleted());
    assertIteration(rows(1,1), d.inserted());
  }
  
  @Test 
  public void testDiff5() {
    Delta d = new Delta(rows(1,1), empty());
    assertIteration(rows(1,1), d.deleted());
    assertIteration(empty(), d.inserted());
  }
  
  @Test 
  public void testDiff6() {
    Delta d = new Delta(rows(1,2,3,4,5,6,7), rows(8,1,2,3,8,9));
    assertIteration(rows(4,5,6,7), d.deleted());
    assertIteration(rows(8,8,9), d.inserted());
  }
  
  @Test 
  public void testDiff7() {
    Delta d = new Delta(rows(7,7,6,5,4,3,2,1,0), rows(8,1,2,3,4,5,6,7));
    assertIteration(rows(7,0), d.deleted());
    assertIteration(rows(8), d.inserted());
  }
  
  @Test 
  public void testDiff8() {
    Delta d = new Delta(rows(7,7,6,7,6,5,4,3,2,1,0), rows(1,2,3,4));
    assertIteration(rows(7,7,7,6,6,5,0), d.deleted());
    assertIteration(empty(), d.inserted());
  }
  
  @Test 
  public void testDiff9() {
    Delta d = new Delta(rows(1,2,3,4), rows(7,7,6,7,6,5,4,3,2,1,0));
    assertIteration(empty(), d.deleted());
    assertIteration(rows(7,7,7,6,6,5,0), d.inserted());
  }
}
