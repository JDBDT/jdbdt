package org.jdbdt;

import static org.jdbdt.JDBDT.*;
import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("javadoc")
public class TearDownTest extends DBTestCase {

  private static Table table;

  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = 
      table(UserDAO.TABLE_NAME)
        .boundTo(getConnection());
  }
  
  @Test
  public void testTruncate() throws SQLException {
    truncate(table);
    assertEquals(0, getDAO().count());
  }

  @Test
  public void testDeleteAll1() throws SQLException {
    deleteAll(table);
    assertEquals(0, getDAO().count());
  }
  
  @Test
  public void testDeleteAll2() throws SQLException {
    int n = getDAO().count();
    deleteAll(selectFrom(table).where("LOGIN='" + EXISTING_DATA_ID1+"'"));
    assertEquals(n-1, getDAO().count());
    assertNull(getDAO().query(EXISTING_DATA_ID1));
  }
  
  @Test
  public void testDeleteAll3() throws SQLException {
    int n = getDAO().count();
    deleteAll(selectFrom(table).where("LOGIN=?"), EXISTING_DATA_ID1);
    assertEquals(n-1, getDAO().count());
    assertNull(getDAO().query(EXISTING_DATA_ID1));
  }
  
  @Test(expected=InvalidUsageException.class)
  public void testDeleteAll4() throws SQLException {
    deleteAll(selectFrom(table));
  }
  
  @Test(expected=InvalidUsageException.class)
  public void testDeleteAll5() throws SQLException {
    deleteAll(selectFrom(table).where("w").groupBy("g"));
  }
  
  @Test(expected=InvalidUsageException.class)
  public void testDeleteAll6() throws SQLException {
    deleteAll(selectFrom(table).where("w").having("h"));
  }
}
