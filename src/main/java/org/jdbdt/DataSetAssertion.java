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

import java.util.Iterator;

/**
 * Data set assertion.
 * 
 * @since 1.0
 */
final class DataSetAssertion {
  /**
   * Iterator type enumeration.
   */
  enum IteratorType {
    /** Expected (in full). */
    EXPECTED_DATA,
    /** Expected data not matched. */
    ERRORS_EXPECTED,
    /**  Actual data not matched. */
    ERRORS_ACTUAL
  }
  
  /**
   * 'Old' data expected.
   */
  private final DataSet data;
 
  /**
   * Delta for actual match.
   */
  private final Delta delta;
  
  
  /**
   * Constructor.
   * @param data Expected data
   * @param delta Actual delta (empty if assertion passes)
   */
  DataSetAssertion(DataSet data, Delta delta) {
    this.data = data;
    this.delta = delta;
  }
  
  /**
   * Check if the assertion passed.
   * @return <code>true</code> If the assertion passed.
   */
  boolean passed() {
    return delta.isEmpty();
  }

  /**
   * Get information for the assertion.
   * @param type Type of iterator
   * @return An iterator for the specified data.
   */
  Iterator<Row> data(IteratorType type) {
    Iterator<Row> itr;
    switch(type) {
      case EXPECTED_DATA:
        itr = data.getRows().iterator();
        break;
      case ERRORS_EXPECTED:
        itr = delta.deleted();
        break;
      case ERRORS_ACTUAL:
        itr = delta.inserted();
        break;
      default:
        throw new InternalErrorException("Unexpected case!");      
    }
    return itr;
  }

  /**
   * Get data source.
   * @return Data source associated to state assertion.
   */
  public DataSource getSource() {
    return data.getSource();
  }
  
}
