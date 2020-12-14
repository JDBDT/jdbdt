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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(TestCategories.CSVSupport.class)
public class CSVParseTest {

  static final CSV.Format FORMAT = new CSV.Format();

  void mustFail(String input, int n) {
    assertFalse(FORMAT.read(input, new Object[n]));
  }

  
  @Test
  public void fai11() {
    mustFail("", 2); 
    mustFail("\"\"", 2); 
  }
  
  @Test
  public void fai12() {
    mustFail("x", 2); 
    mustFail("\"x\"", 2); 
  }
  
  @Test
  public void fai13() {
    mustFail("x,y,z", 2); 
    mustFail("x,y,\"z\"", 2); 
    mustFail(",,", 2); 
    mustFail("\"\",\"\",\"\"", 2); 
  }
  
  
  @Test
  public void fai14() {
    mustFail("\"x", 1); 
    mustFail("x,\"y", 2); 
  }

  
  void test(String input, String... expected) {
    Object[] actual = new Object[expected.length];
    boolean b = FORMAT.read(input, actual);
    assertArrayEquals(expected, actual);
    assertTrue(b);
  }

  @Test
  public void test1a() {
    test("", "");
  }

  @Test
  public void test1b() {
    test("\"\"", "");
  }

  @Test
  public void test1c() {
    test("x", "x");
  }

  @Test
  public void test1d() {
    test("\"x,y\"", "x,y");
  }

  @Test
  public void test1e() {
    test(",", "","");
  }

  @Test
  public void test1f() {
    test("\"\",\"\"", "","");
  }

  @Test
  public void test2a() {
    test("a,b,c", "a", "b", "c");
  }

  @Test
  public void test2b() {
    test("\"a\",b,c", "a", "b", "c");
  }
  @Test
  public void test2c() {
    test("a,b,\"c\"", "a", "b", "c");
  }
  @Test
  public void test2d() {
    test("a,\"b\",c", "a", "b", "c");
  }
  @Test
  public void test2e() {
    test("\"a\",\"b\",\"c\"", "a", "b", "c");
  }
  @Test
  public void test3a() {
    test("a,b,c", "a", "b", "c");
  }
  @Test
  public void test3b() {
    test("\"a,b\",c,d", "a,b", "c", "d");
  }
  @Test
  public void test3c() {
    test("a,b,\"c,d\"", "a", "b", "c,d");
  }
  @Test
  public void test3d() {
    test("a,\"b,c\",d", "a", "b,c", "d");
  }
  @Test
  public void test3e() {
    test("\"a,b\",c,\"d,e\"", "a,b", "c", "d,e");
  }
  @Test
  public void test4a() {
    test(",a,b,c", "", "a", "b", "c");
  }
  @Test
  public void test4b() {
    test("\"a,b\",c,d,", "a,b", "c", "d","");
  }
  @Test
  public void test4c() {
    test("a,b,,\"c,d\"", "a", "b", "", "c,d");
  }
  @Test
  public void test4d() {
    test("\"\",a,\"b,c\",d", "","a", "b,c", "d");
  }
  @Test
  public void test4e() {
    test("\"a,b\",c,\"d,e\",\"\"", "a,b", "c", "d,e","");
  }
  @Test
  public void test4f() {
    test("a,b,\"\",,,\"c,d\"", "a", "b", "", "", "", "c,d");
  }
  @Test
  public void testRandom() {
    Random rng = new Random(0);
    for (int c = 0; c < 100; c++) {
      int n = 1 + rng.nextInt(5);
      String[] expected = new String[n];
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < n; i++) {
        if (i > 0) sb.append(',');
        String elem; 
        if (rng.nextBoolean()) {
          sb.append('\"');
          switch (rng.nextInt(3)) {
            case 0:
              elem = "";
              break;
            case 1:
              elem = "x";
              break;
            case 2: 
            default:
              elem = "x,y";
          }
          sb.append(elem);
          sb.append('\"');
        } else {
          elem = rng.nextBoolean() ? "x" : "";
          sb.append(elem);
        }
        expected[i] = elem;
      }
      test(sb.toString(), expected);
    }
  }
  

}
