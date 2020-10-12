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
