/*
 * The MIT License
 *
 * Copyright (c) 2016-2019 Eduardo R. B. Marques
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

/**
 * Simple assertion.
 * 
 * @since 1.0
 */
final class SimpleAssertion {
  /**
   * Data source this assertion relates to.
   */
  private final DataSource dataSource;
  
  /**
   * Expected data.
   */
  private final Object expected;
 
  /**
   * Actual data.
   */
  private final Object actual;
  
  
  /**
   * Constructor.
   * @param dataSource Data source.
   * @param expected Expected data.
   * @param actual Actual data.
   */
  SimpleAssertion(DataSource dataSource, Object expected, Object actual) {
    this.dataSource = dataSource;
    this.expected = expected;
    this.actual = actual;
  }
  
  /**
   * Check if the assertion passed.
   * @return <code>true</code> If the assertion passed.
   */
  boolean passed() {
    return expected.equals(actual);
  }

  
  /**
   * Get data source.
   * @return Data source associated to state assertion.
   */
  DataSource getSource() {
    return dataSource;
  }
  
  /**
   * Get expected result.
   * @return The expected result.
   */
  Object getExpectedResult() {
    return expected;
    
  }
  
  /**
   * Get expected result.
   * @return The expected result.
   */
  Object getActualResult() {
    return actual;
  }
}
