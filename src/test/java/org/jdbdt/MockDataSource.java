package org.jdbdt;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
@SuppressWarnings("javadoc")
class MockDataSource extends DataSource {
  MockDataSource(String... columns) {
    super(new DB(mockConnection()), columns);
  }
  
  private static Connection mockConnection() {
    try {
      DatabaseMetaData mdmd = mock(DatabaseMetaData.class);
      when(mdmd.supportsBatchUpdates()).thenReturn(false);
      when(mdmd.supportsSavepoints()).thenReturn(false);
      
      Connection mc = mock(Connection.class);
      when(mc.getMetaData()).thenReturn(mdmd);
      return mc;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } 
  }
}
