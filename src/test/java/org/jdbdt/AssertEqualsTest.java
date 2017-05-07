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
