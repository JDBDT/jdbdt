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

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Statement wrapper.
 *
 * @since 1.0
 */
final class WrappedStatement implements AutoCloseable {
  /**
   * Wrapped statement.
   */
  private final PreparedStatement statement;
  
  /**
   * Reuse flag.
   */
  private final boolean reuse;

  /**
   * Constructor.
   * @param statement Statement.
   * @param reuse Flag indicating if statement will be reused.
   */
  WrappedStatement(PreparedStatement statement, boolean reuse) {
    this.statement = statement;
    this.reuse = reuse;
  }

  /**
   * Get wrapped statement.
   * @return The prepared statement.
   */
  PreparedStatement getStatement() {
    return statement;
  }

  /**
   * Close.
   * If the wrapped statement is reusable, it will only call {@link PreparedStatement#clearParameters()},
   * otherwise it will call {@link PreparedStatement#close()}.
   */
  @Override
  public void close() throws SQLException {
    if (reuse) {
      statement.clearParameters();
    } else {
      statement.close();
    }
  }
}
