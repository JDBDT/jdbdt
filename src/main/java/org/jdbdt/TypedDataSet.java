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

import java.util.Collection;

/**
 * Typed data set.
 * @param <T> Type of objects.
 *
 * @see JDBDT#data(DataSource, Conversion)
 * @see Conversion
 * 
 * @since 1.0
 */
public final class TypedDataSet<T> extends DataSet {
  /**
   * Conversion function.
   */
  private final Conversion<T> conv;
  
  /**
   * Construct a typed data set.
   * @param ds Data source.
   * @param conv Conversion function.
   */
  public TypedDataSet(DataSource ds, Conversion<T> conv) {
    super(ds);
    this.conv = conv;
  }
  
  /**
   * Add row to the data set.
   * @param rowObj Row object.
   * @return The data set instance (for possible chaining).
   */
  public TypedDataSet<T> row(T rowObj) {
    super.row(conv.convert(rowObj));
    return this;
  }

  /**
   * Add rows to the data set.
   * @param rows Objects to map onto row values.
   * @return The data set instance (for possible chaining).
   */
  @SafeVarargs
  public final TypedDataSet<T> rows(T... rows)  {
    for (T rowObj : rows) {
      super.row(conv.convert(rowObj));
    }
    return this;
  }

  /**
   * Add rows in a collection to the data set.
   * @param rows Collection of row objects.
   * @return The data set instance (for possible chaining).
   */
  public TypedDataSet<T> rows(Collection<? extends T> rows) {
    for (T rowObj : rows) {
      super.row(conv.convert(rowObj));
    }
    return this;
  }
}
