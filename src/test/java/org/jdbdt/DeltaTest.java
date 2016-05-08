package org.jdbdt;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeltaTest {

  @Rule public TestName testName = new TestName();

  Iterator<Row> empty() {
    return Collections.emptyIterator();
  }
  
  Delta emptyDelta() {
    return new Delta(empty(),
                     empty());
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
      //Row r;
      assertEquals(exp.next(), actual.next()); 
      //System.out.println(r + " " + actual.hasNext() + " " +( actual.hasNext() ? actual.next() : ""));
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
}
