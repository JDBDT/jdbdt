/*
 * The MIT License
 *
 * Copyright (c) 2016-2018 Eduardo R. B. Marques
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
 * Delta assertion.
 * 
 * @since 1.0
 */
final class DeltaAssertion {
  /**
   * Iterator type enumeration.
   */
  enum IteratorType {
    /** 'Old' data expected (in full). */
    OLD_DATA_EXPECTED,
    /** 'New' data expected (in full). */
    NEW_DATA_EXPECTED,
    /** Expected 'old' data not matched. */
    OLD_DATA_ERRORS_EXPECTED,
    /**  Actual 'old' data not matched. */
    OLD_DATA_ERRORS_ACTUAL,
    /**  Expected 'new' data not matched. */
    NEW_DATA_ERRORS_EXPECTED,
    /**  Actual 'new' data not matched. */
    NEW_DATA_ERRORS_ACTUAL
  }
  
  /**
   * 'Old' data expected.
   */
  private final DataSet oldData;

  /**
   * 'New' data expected.
   */
  private final DataSet newData;
  
  /**
   * Delta for actual match of 'old' data.
   */
  private final Delta oldDataMatch;
  
  /**
   * Delta for actual match of 'new' data.
   */
  private final Delta newDataMatch;
  
  /**
   * Constructor.
   * @param oldData 'old' data expected
   * @param newData 'new' data expected
   * @param oldDataMatch 'old' data delta
   * @param newDataMatch 'new' data delta
   */
  DeltaAssertion(DataSet oldData, DataSet newData, Delta oldDataMatch,
      Delta newDataMatch) {
    this.oldData = oldData;
    this.newData = newData;
    this.oldDataMatch = oldDataMatch;
    this.newDataMatch = newDataMatch;
  }
  
  /**
   * Check if the assertion passed.
   * @return <code>true</code> If the assertion passed.
   */
  boolean passed() {
    return oldDataMatch.isEmpty() && newDataMatch.isEmpty();
  }

  /**
   * Get information for the assertion.
   * @param type Type of iterator
   * @return An iterator for the specified data.
   */
  Iterator<Row> data(IteratorType type) {
    Iterator<Row> itr;
    switch(type) {
      case OLD_DATA_EXPECTED:
        itr = oldData.getRows().iterator();
        break;
      case NEW_DATA_EXPECTED:
        itr = newData.getRows().iterator();
        break;
      case OLD_DATA_ERRORS_EXPECTED:
        itr = oldDataMatch.deleted();
        break;
      case OLD_DATA_ERRORS_ACTUAL:
        itr = oldDataMatch.inserted();
        break;
      case NEW_DATA_ERRORS_EXPECTED:
        itr = newDataMatch.deleted();
        break;
      case NEW_DATA_ERRORS_ACTUAL:
        itr = newDataMatch.inserted();
        break;
      default:
        throw new InternalErrorException("Unexpected case!");      
    }
    return itr;
  }

  /**
   * Get data source.
   * @return Data source associate to the assertion.
   */
  DataSource getSource() {
    return oldData.getSource();
  }
}
