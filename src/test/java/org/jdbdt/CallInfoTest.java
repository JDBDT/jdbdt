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

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CallInfoTest {

  @Rule public TestName testName = new TestName();

  private static final String FILENAME = CallInfoTest.class.getSimpleName() + ".java";
  
  private static class FakeClass {
    static CallInfo fake() { 
      return CallInfo.create(); 
    }
    
    static CallInfo fake(String msg) { 
      return CallInfo.create(msg); 
    }
  }
  
  static void assertMethodInfo(CallInfo.MethodInfo ci, String className, String methodName) {
    assertEquals("class name", className, ci.getClassName());
    assertEquals("method name", methodName, ci.getMethodName());
    assertEquals("file name", FILENAME, ci.getFileName());
  }

  void assertCallInfo(CallInfo ci, String msg) {
    assertMethodInfo(ci.getCallerMethodInfo(),
                     getClass().getName(),
                     testName.getMethodName());
    assertMethodInfo(ci.getAPIMethodInfo(), 
                     FakeClass.class.getName(),
                     "fake");
    assertEquals("message", msg, ci.getMessage());
  }
  
  @Test
  public void test1() {
    assertCallInfo(FakeClass.fake(), "");
  }

  @Test
  public void test2() {
    String msg = "alpha beta gamma delta";
    assertCallInfo(FakeClass.fake(msg), msg);
  }
}
