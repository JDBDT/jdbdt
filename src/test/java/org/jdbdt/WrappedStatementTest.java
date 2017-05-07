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
