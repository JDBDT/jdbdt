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

import java.sql.SQLException;

/**
 * Exception thrown due to a database error.
 * 
 * <p>
 * An exception of this type wraps an instance of 
 * {@link SQLException} (a checked exception type)
 * that occurred while executing a database operation
 * issued by JDBDT. 
 * </p>
 * 
 * @since 1.0
 */
public final class DBExecutionException extends JDBDTRuntimeException {
  /**
   * Constructs the exception with given cause.
   * @param cause Cause for the exception.
   */
  public DBExecutionException(SQLException cause) {
    super("Database execution error", cause);
  }
  
  /**
   * Get cause of the exception. 
   * 
   * @return An instance of {@link java.sql.SQLException}.
   */
  @Override
  public SQLException getCause() {
    return (SQLException) super.getCause();
  }
  
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;
}
