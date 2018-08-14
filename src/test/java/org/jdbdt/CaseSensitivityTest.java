package org.jdbdt;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.jdbdt.JDBDT.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CaseSensitivityTest extends DBTestCase {  
  
  private Table table;
  private static final String TNAME = "FOO";
  private static final String CNAME1 = "\"xYz\"";
  private static final String CNAME2 = "ABC";

  @Test @Category(TestCategories.CaseSensitive.class)
  public void test() throws SQLException {
    DB db = getDB();
    db.enable(DB.Option.CASE_SENSITIVE_COLUMN_NAMES);
    Statement s = db.getConnection().createStatement();
    
    try {
      s.execute(String.format("DROP TABLE %s",TNAME));
    }
    catch(SQLException e) { 
      
    }
    
    s.execute(String.format("CREATE TABLE %s ( %s INTEGER, %s INTEGER)", TNAME, CNAME1, CNAME2));
    s.execute(String.format("INSERT INTO %s ( %s, %s ) VALUES(0, 0)", TNAME, CNAME1, CNAME2));
    s.execute(String.format("INSERT INTO %s ( %s, %s ) VALUES(1, 1)", TNAME, CNAME1, CNAME2));
    s.execute(String.format("INSERT INTO %s ( %s, %s ) VALUES(2, 2)", TNAME, CNAME1, CNAME2));
    table = table(TNAME).columns(CNAME1, CNAME2).key(CNAME1).build(db);
    
    List<String> cols = table.getColumns();
    assertEquals(2, cols.size());
    assertEquals(CNAME1, cols.get(0));
    assertEquals(CNAME2, cols.get(1));

    takeSnapshot(table);
    DataSet data = builder(table).sequence(CNAME1, 3)
                                 .sequence(CNAME2, 3)
                                 .generate(3).data();
    insert(data);
    assertInserted(data);
    
    delete(data);
    assertUnchanged(table);
  }
  
}
