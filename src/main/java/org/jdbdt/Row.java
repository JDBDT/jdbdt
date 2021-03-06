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

import java.util.Arrays;

/**
 * Database row representation.
 * 
 * <p>
 * This class is used internally by JDBDT
 * to represent database rows.
 * </p>
 * 
 * @since 1.0
 */
final class Row {
  /**
   * Data.
   */
  private final Object[] data;
  /**
   * Hash code (computed only once).
   */
  private int hash; 
  
  /**
   * Constructs a new row.
   * @param data Values for the columns of this row.
   */
  Row(Object[] data) {
    this.data = data;
    this.hash = 0;
  }
  
  /**
   * Get data for this row.
   * @return The data representing the columns of this row.
   */
  Object[] data() {
    return data;
  }
  
  /**
   * Get row length.
   * @return the number of columns in the database row.
   */
  int length() {
    return data.length;
  }
  
  /**
   * Test for equality.
   * @return <code>true</code> if <code>o</code> represents a row with the same data.
   */
  @Override
  public boolean equals(Object o) {
    return  o == this 
        || (o instanceof Row && Arrays.deepEquals(data, ((Row) o).data));
  }
  
  /**
   * Get hash code.
   * 
   * 
   * @return Hash code for this row obtained by calling 
   *        {@link Arrays#deepHashCode(Object[])} over {@link #data()}.
   *        The returned value is calculated only once and cached for subsequent calls.    
   */
  @Override
  public int hashCode() {
    int h = hash;
    if (h == 0) {
      hash = h = Arrays.deepHashCode(data);
    }
    return h;
  }
  
  /**
   * Get string representation for this row.
   * @return String obtained by calling {@link Arrays#deepToString(Object[])} over {@link #data()}.
   */
  @Override
  public String toString() {
    return Arrays.deepToString(data);
  }

}