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
 * Query data source.
 *
 * @see JDBDT#query(DB, String, Object...)
 * @see JDBDT#select(String...)
 * @see QueryBuilder
 * 
 * @since 1.0 
 *
 */
public final class Query extends DataSource {
  /**
   * Constructor.
   * @param db Database handle.
   * @param sql SQL for query
   * @param args Optional arguments for query.
   */
  public Query(DB db, String sql, Object... args) {
    this(CallInfo.create(), db, sql, args);
  }
  
  /**
   * Constructor with supplied call information.
   * @param callInfo Call info.
   * @param db Database handle.
   * @param sql SQL for query
   * @param args Optional arguments for query.
   */
  Query(CallInfo callInfo, DB db, String sql, Object... args) {
    super(callInfo, db, sql, args);
  }
}
