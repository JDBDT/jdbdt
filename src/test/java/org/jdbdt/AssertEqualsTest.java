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

import static org.jdbdt.JDBDT.assertEquals;
import static org.jdbdt.JDBDT.table;

import java.util.Random;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AssertEqualsTest extends DBTestCase {

  Table table;
  
  @Before
  public void setup() {
    table = table(UserDAO.TABLE_NAME).columns(UserDAO.COLUMNS).build(getDB());
  }
 
  @Test
  public void testEmptyVsEmpty() {
    assertEquals(d(0), d(0));
  }
  
  @Test
  public void testEmptyVsEmpty2() {
    assertEquals("msg", d(0), d(0));
  }
  
  @Test(expected=DBAssertionError.class)
  public void testEmptyVsNonEmpty() {
    assertEquals(d(0), d(1));
  }
  
  @Test(expected=DBAssertionError.class)
  public void testNonEmptyVEmpty() {
    assertEquals(d(1), d(0));
  }
  
  @Test
  public void testWithSameExactOrder() {
    User[] users = users(10);
    assertEquals(d(users), d(users));
  }
  
  @Test
  public void testValidateErrorMsg1() {
    TestUtil.expectAssertionError("", () -> assertEquals(d(0), d(1)));
  }
  
  @Test
  public void testValidateErrorMsg2() {
    TestUtil.expectAssertionError("msg", () -> assertEquals("msg", d(0), d(1)));
  }
  
  @Test(expected=DBAssertionError.class)
  public void testSlightChangeDetection1() {
    int n = 10;
    User[] a = users(n);
    User[] b = copy(a);
    mutate(b[0]);
    assertEquals(d(a), d(b));
  }
  
  @Test(expected=DBAssertionError.class)
  public void testSlightChangeDetection2() {
    int n = 10;
    User[] a = users(n);
    User[] b = copy(a);
    mutate(b[n-1]);
    assertEquals(d(a), d(b));
  }
  
  @Test
  public void testWithPermutedOrder() {
    int n = 1000;
    User[] a = users(n);
    User[] b = permutation(a); 
    assertEquals(d(a), d(b));
  }
  
  @Test(expected=DBAssertionError.class)
  public void testSlightChangeInPermutedOrder() {
    int n = 1000;
    User[] a = users(n);
    User[] b = permutation(a); 
    mutate(b[n-1]);
    assertEquals(d(a), d(b));
  }
  
  
  private DataSet d(User... values) {
    DataSet data =  new DataSet(table);
    for (User u : values) {
      data.row(getConversion().convert(u));
    }
    return data;
  }
  
  private DataSet d(int n) {
    return d(users(n));
  }
  
  private User[] users(int n) {
    User[] users = new User[n];
    for (int i = 0; i < n; i++) {
      users[i] = buildNewUser();
    }
    return users;
  }
  
  private User[] copy(User[] u) {
    User[] c = new User[u.length];
    for (int i = 0; i < u.length; i++) {
      c[i] = u[i].clone();
    }
    return c;
  }
  
  private User[] permutation(User[] u) {
    User[] p = copy(u);
    int n = p.length;
    Random rng = new Random(u[0].getName().hashCode());
    for (int i = 0; i < n; i++) {
      int j = rng.nextInt(n);
      User tmp = p[i];
      p[i] = p[j];
      p[j] = tmp;
    }
    return p;
  }
  
  private void mutate(User u) {
    u.setName(u.getName() + "x");
  }
  
 
}
