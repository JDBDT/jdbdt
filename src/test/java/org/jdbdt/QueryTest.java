package org.jdbdt;


import static org.jdbdt.JDBDT.*;

import java.sql.SQLException;
import java.util.EnumMap;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryTest extends DBTestCase { 
  enum S {
    ARGS, COLS, GROUP_BY, HAVING, ORDER_BY, WHERE;
  }
  interface QMutator<T> {
    Query set(T arg);
  }
  private Query theSUT;
  private EnumMap<S,Object> qsetup;
 
  @Before
  public void setup() throws SQLException {
    Table t = table(UserDAO.TABLE_NAME)
             .columns(UserDAO.COLUMNS)
             .boundTo(getConnection());
    theSUT = selectFrom(t);
    qsetup = new EnumMap<>(S.class);
    for (S s : S.values()) {
      qsetup.put(s, null);
    }
    qsetup.put(S.COLS, UserDAO.COLUMNS);
  }
  
  <T> void setup(S s, QMutator<T> m, T arg) {
    qsetup.put(s, arg);
    assertSame(theSUT, m.set(arg));
  }

  void verifyQSettings() {
    assertEquals(qsetup.get(S.WHERE),    theSUT.whereClause());
    assertEquals(qsetup.get(S.HAVING),   theSUT.havingClause());
    assertArrayEquals((String[]) qsetup.get(S.GROUP_BY), s2a(theSUT.groupByClause()));
    assertArrayEquals((String[]) qsetup.get(S.ORDER_BY), s2a(theSUT.orderByClause()));
    assertArrayEquals((Object[]) qsetup.get(S.ARGS), theSUT.getQueryArguments());
    assertArrayEquals((String[]) qsetup.get(S.COLS), theSUT.getColumnNames());
  }
  
  static String[] s2a(String s) {
    return s == null ? null : s.split(",");
  }
  
  @Test
  public void testInit() {
    verifyQSettings();
  }
  
  @Test
  public void testInitWhere() {
    setup(S.WHERE, theSUT::where, "login='foo'");
    verifyQSettings();
  }
  
  @Test
  public void testInitOrderBy1() {
    setup(S.ORDER_BY, theSUT::orderBy, new String[] { "login" });
    verifyQSettings();
  }
  
  @Test
  public void testInitOrderBy2() {
    setup(S.ORDER_BY, theSUT::orderBy, new String[] { "password", "login" });
    verifyQSettings();
  }
  
  
  @Test
  public void testInitGroupBy1() {
    setup(S.GROUP_BY, theSUT::groupBy, new String[] { "password" });
    verifyQSettings();
  }
  
  @Test
  public void testInitGroupBy2() {
    // TODO sensible query
    setup(S.GROUP_BY, theSUT::groupBy, new String[] { "password", "login" });
    verifyQSettings();
  }
  
  @Test
  public void testInitHaving() {
    // TODO sensible query
    setup(S.HAVING, theSUT::having, "created NOT NULL");
    verifyQSettings();
  }
  @Test
  public void testInitQueryArguments() {
    setup(S.ARGS, theSUT::withArguments, new Object[] { "foo", 1 });
    verifyQSettings();
  }
  
  @Test
  public void testInitChain1() {
    setup(S.WHERE, theSUT::where, "login LIKE '%user%'");
    setup(S.ORDER_BY, theSUT::orderBy, new String[] { "login" });
    verifyQSettings();
  }
  
  
  @Test
  public void testInitChain2() {
    // TODO sensible query
    setup(S.WHERE, theSUT::where, "login LIKE ?");
    setup(S.ARGS, theSUT::withArguments, new Object[] { "foo%" });
    setup(S.ORDER_BY, theSUT::orderBy, new String[] { "login" });
    setup(S.HAVING, theSUT::having, "created NOT NULL");
    
    verifyQSettings();
  }
  
  
  void initTwice(QMutator<String> m) {
    m.set("1");
    try {
      m.set("2");
      fail(InvalidUsageException.class.toString());
    } 
    catch(InvalidUsageException e) { }
  }
  
  @Test 
  public void testInitWhereTwice() { 
    initTwice(theSUT::where); 
  }
  @Test 
  public void testInitGroupByTwice() {
    initTwice(theSUT::groupBy);
  }
  @Test 
  public void testInitOrderByTwice() {
    initTwice(theSUT::orderBy);
  }
  @Test 
  public void testInitHavingTwice() {
    initTwice(theSUT::having);
  }
  @Test 
  public void testInitArgumentsTwice() {
    initTwice(theSUT::withArguments);
  }
  @Test 
  public void testInitColumnsTwice() {
    initTwice(theSUT::columns);
  }
  
  void initAfterCompiling(QMutator<String> m) {
    theSUT.getQueryStatement();
    try {
      m.set("x");
      fail(InvalidUsageException.class.toString());
    } 
    catch(InvalidUsageException e) { }
  }
  
  @Test 
  public void testInitWhereAfterCompiling() { 
    initAfterCompiling(theSUT::where); 
  }
  @Test 
  public void testInitGroupByAfterCompiling() {
    initAfterCompiling(theSUT::groupBy);
  }
  @Test 
  public void testInitOrderByAfterCompiling() {
    initAfterCompiling(theSUT::orderBy);
  }
  @Test 
  public void testInitHavingAfterCompiling() {
    initAfterCompiling(theSUT::having);
  }
  @Test 
  public void testInitArgumentsAfterCompiling() {
    initAfterCompiling(theSUT::withArguments);
  }
  @Test 
  public void testInitColumnsAfterCompiling() {
    initAfterCompiling(theSUT::columns);
  }
}
