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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WrappedStatementTest {

  @Test
  public void testReusableStatement() throws SQLException {
    PreparedStatement mockStmt = mock(PreparedStatement.class);
    WrappedStatement ws = new WrappedStatement(mockStmt, true);
    ws.close();
    assertSame(mockStmt, ws.getStatement());
    verify(mockStmt).clearParameters();
  }
  
  
  @Test
  public void testNonReusableStatement() throws SQLException {
    PreparedStatement mockStmt = mock(PreparedStatement.class);
    WrappedStatement ws = new WrappedStatement(mockStmt, false);
    ws.close();
    assertSame(mockStmt, ws.getStatement());
    verify(mockStmt).close();
  }
  
}
