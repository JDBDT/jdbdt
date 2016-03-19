package org.jdbdt;


import static org.jdbdt.JDBDT.*;

import java.sql.SQLException;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryTest extends DBTestCase {  
  private Query theSUT;
  
  @Before
  public void setup() throws SQLException {
    Table t = table(UserDAO.TABLE_NAME)
             .columns(UserDAO.COLUMNS)
             .boundTo(getConnection());
    theSUT = selectFrom(t);
  }

  @Test
  public void testInit() {
    assertNull(theSUT.whereClause());
    assertNull(theSUT.groupByClause());
    assertNull(theSUT.havingClause());
    assertNull(theSUT.orderByClause());
  }
  
  @Test
  public void testInitWhere() {
    String clause = "login='foo'";
    theSUT.where(clause);
    assertEquals(clause, theSUT.whereClause());
    assertNull(theSUT.groupByClause());
    assertNull(theSUT.havingClause());
    assertNull(theSUT.orderByClause());
  }
  
  @Test
  public void testInitOrderBy1() {
    String clause = "login";
    theSUT.orderBy(clause);
    assertNull(theSUT.whereClause());
    assertNull(theSUT.groupByClause());
    assertNull(theSUT.havingClause());
    assertEquals(clause, theSUT.orderByClause());
    assertNull(theSUT.getQueryArguments());
  }
  
  @Test
  public void testInitOrderBy2() {
    String c1 = "login", c2 = "password";
    theSUT.orderBy(c1, c2);
    assertNull(theSUT.whereClause());
    assertNull(theSUT.groupByClause());
    assertNull(theSUT.havingClause());
    assertEquals(c1 + "," + c2, theSUT.orderByClause());
    assertNull(theSUT.getQueryArguments());
  }
  
  @Test
  public void testInitGroupBy1() {
    // TODO sensible query
    String clause = "login";
    theSUT.groupBy(clause);
    assertNull(theSUT.whereClause());
    assertEquals(clause, theSUT.groupByClause());
    assertNull(theSUT.havingClause());
    assertNull(theSUT.orderByClause());
    assertNull(theSUT.getQueryArguments());
  }
  
  @Test
  public void testInitGroupBy2() {
    // TODO sensible query
    String c1 = "login", f2 = "password";
    theSUT.groupBy(c1, f2);
    assertNull(theSUT.whereClause());
    assertEquals(c1 + "," + f2, theSUT.groupByClause());
    assertNull(theSUT.havingClause());
    assertNull(theSUT.orderByClause());
    assertNull(theSUT.getQueryArguments());
  }
  
  @Test
  public void testInitHaving() {
    String clause = "created NOT NULL";
    theSUT.having(clause);
    assertNull(theSUT.whereClause());
    assertNull(theSUT.groupByClause());
    assertEquals(clause,  theSUT.havingClause());
    assertNull(theSUT.orderByClause());
    assertNull(theSUT.getQueryArguments());
  }
  
  @Test
  public void testInitChain1() {
    // TODO sensible query
    String wClause ="login LIKE '%user%'", 
           obClause = "login";
    theSUT.where(wClause ).orderBy(obClause);
    assertEquals(wClause , theSUT.whereClause());
    assertNull(theSUT.groupByClause());
    assertNull(theSUT.havingClause());
    assertEquals(obClause, theSUT.orderByClause());
    assertNull(theSUT.getQueryArguments());
  }
  
  
  @Test
  public void testInitChain2() {
    // TODO sensible query
    String w ="login LIKE '%user%'", 
           gb = "login",
           h = "created NOT NULL",
           ob = "login";
           
    theSUT.where(w)
          .groupBy(gb)
          .having(h)
          .orderBy(ob);
    
    assertEquals(w , theSUT.whereClause());
    assertEquals(gb, theSUT.groupByClause());
    assertEquals(h, theSUT.havingClause());
    assertEquals(ob, theSUT.orderByClause());
    assertNull(theSUT.getQueryArguments());
  }
  

  
  @Test
  public void testInitQueryArguments1() {
    // TODO sensible query
    String w ="login LIKE ?"; 
    Object args[] = { "foo" };
    theSUT.where(w).withArguments(args);
    assertEquals(w , theSUT.whereClause());
    assertNull(theSUT.groupByClause());
    assertNull(theSUT.havingClause());
    assertNull(theSUT.orderByClause());
    assertArrayEquals(args, theSUT.getQueryArguments());
  }
  
  @Test
  public void testInitQueryArguments2() {
    // TODO sensible query
    String w ="login LIKE ? AND PASSWORD LIKE ?"; 
    Object args[] = { "foo", "foo" };
    theSUT.where(w).withArguments(args);
    assertEquals(w , theSUT.whereClause());
    assertNull(theSUT.groupByClause());
    assertNull(theSUT.havingClause());
    assertNull(theSUT.orderByClause());
    assertArrayEquals(args, theSUT.getQueryArguments());
  }
  
  @Test
  public void testInitColumns1() {
    String[] cols = { "login", "password" };
    theSUT.columns(cols);
    assertArrayEquals(cols, theSUT.getColumnNames());
  }
  
  interface QueryChainMethod {
    Query exec(String arg);
  }
  
  void initTwice(QueryChainMethod m) {
    m.exec("first");
    try {
      m.exec("again");
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
  
  void initAfterCompiling(QueryChainMethod m) {
    theSUT.getQueryStatement();
    try {
      m.exec("x");
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
