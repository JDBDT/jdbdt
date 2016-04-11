package org.jdbdt;

import static org.junit.Assert.fail;


@SuppressWarnings("javadoc")
class TestUtil {

  interface Thrower {
    void run() throws Throwable;
  }
  
  static void 
  expectException(Class<? extends Throwable> excClass, Thrower thrower) {
    try {
      thrower.run();      
    } 
    catch (Throwable e) {
      if (! excClass.isAssignableFrom(e.getClass())) {
        fail("Expected " + excClass.getName() + 
              " but " + e.getClass().getName() + 
              " was thrown instead");
      }
      return;
    }
    fail("Expected " + excClass.getName() +
         " but no exception was thrown");
  }

}
