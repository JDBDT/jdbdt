package org.jdbdt;


import static org.jdbdt.JDBDT.*;
import static org.jdbdt.TestUtil.*;
import org.jdbdt.QueryBuilder.Param;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryBuilderTest extends DBTestCase { 

  interface QMutator<T> {
    Query set(T arg);
  }
 // private QueryBuilder theSUT;
  private EnumMap<QueryBuilder.Param,Object> qsetup;

  @Before
  public void createQuery() throws SQLException {
//    theSUT = select(getDB(), UserDAO.COLUMNS)
//            .from(UserDAO.TABLE_NAME);
//    qsetup = new EnumMap<>(Param.class);
//    qsetup.put(Param.FROM, UserDAO.TABLE_NAME);
//    qsetup.put(Param.COLUMNS, UserDAO.COLUMNS);

  }

//  <T> void qset(Param p, QMutator<T> m, T arg) {
//    qsetup.put(p, arg);
//    assertSame(theSUT, m.set(arg));
//  }
//
//  void qverify() {
//    assertEquals(qsetup.get(S.WHERE), theSUT.whereClause());
//    assertEquals(qsetup.get(S.HAVING), theSUT.havingClause());
//    assertEquals(qsetup.get(S.DISTINCT), theSUT.distinctClause());
//    assertArrayEquals((String[]) qsetup.get(S.GROUP_BY), theSUT.groupByClause());
//    assertArrayEquals((String[]) qsetup.get(S.ORDER_BY), theSUT.orderByClause());
//    assertArrayEquals((Object[]) qsetup.get(S.ARGS), theSUT.getQueryArguments());
//    assertArrayEquals((String[]) qsetup.get(S.COLS), theSUT.getColumns());
//  }


//  @Test
//  public void testInit() {
//    qverify();
//  }
//
//  @Test
//  public void testInitWhere() {
//    qset(S.WHERE, theSUT::where, "login='foo'");
//    qverify();
//  }
//
//  @Test
//  public void testInitOrderBy1() {
//    qset(S.ORDER_BY, theSUT::orderBy, new String[] { "login" });
//    qverify();
//  }
//
//  @Test
//  public void testInitOrderBy2() {
//    qset(S.ORDER_BY, theSUT::orderBy, new String[] { "password", "login" });
//    qverify();
//  }
//
//
//  @Test
//  public void testInitGroupBy1() {
//    qset(S.GROUP_BY, theSUT::groupBy, new String[] { "password" });
//    qverify();
//  }
//
//  @Test
//  public void testInitGroupBy2() {
//    qset(S.COLS, theSUT::columns, new String[] { "password", "count(login)" });
//    qset(S.GROUP_BY, theSUT::groupBy, new String[] { "password" });
//    qverify();
//  }
//
//  @Test
//  public void testInitHaving() {
//    qset(S.COLS, theSUT::columns, new String[] { "password", "count(login)" });
//    qset(S.GROUP_BY, theSUT::groupBy, new String[] { "password" });
//    qset(S.HAVING, theSUT::having,  "count(login) > 1" );
//    qverify();
//  }
//
//  @Test
//  public void testInitQueryArguments() {
//    qset(S.ARGS, theSUT::withArguments, new Object[] { "foo", 1 });
//    qverify();
//  }
//
//  @Test
//  public void testInitDistinct() {
//    qset(S.DISTINCT, dummy -> theSUT.distinct(), true);
//    qverify();
//  }
//
//  @Test
//  public void testInitChain() {
//    qset(S.COLS, theSUT::columns, new String[] { "password" });
//    qset(S.WHERE, theSUT::where, "login LIKE ?");
//    qset(S.ARGS, theSUT::withArguments, new Object[] { "foo%" });
//    qset(S.ORDER_BY, theSUT::orderBy, new String[] { "password" });
//    qset(S.DISTINCT, dummy -> theSUT.distinct(), true);
//    qverify();
//  }
//
//  void initTwice(QMutator<String> m) {
//    m.set("1");
//    expectException(InvalidOperationException.class, 
//        () -> m.set("2"));
//  }
//
//  @Test 
//  public void testInitWhereTwice() { 
//    initTwice(theSUT::where); 
//  }
//  @Test 
//  public void testInitGroupByTwice() {
//    initTwice(theSUT::groupBy);
//  }
//  @Test 
//  public void testInitOrderByTwice() {
//    initTwice(theSUT::orderBy);
//  }
//  @Test 
//  public void testInitHavingTwice() {
//    initTwice(theSUT::having);
//  }
//  @Test 
//  public void testInitArgumentsTwice() {
//    initTwice(theSUT::withArguments);
//  }
//  @Test 
//  public void testInitColumnsTwice() {
//    initTwice(theSUT::columns);
//  }
//  @Test 
//  public void testInitDistinctTwice() {
//    initTwice(dummy -> theSUT.distinct());
//  }
//
//  void initAfterCompiling(QMutator<String> m) {
//    theSUT.getQueryStatement();
//    expectException(InvalidOperationException.class,
//        () -> m.set("x"));
//  }
//
//  @Test 
//  public void testInitWhereAfterCompiling() { 
//    initAfterCompiling(theSUT::where); 
//  }
//  @Test 
//  public void testInitGroupByAfterCompiling() {
//    initAfterCompiling(theSUT::groupBy);
//  }
//  @Test 
//  public void testInitOrderByAfterCompiling() {
//    initAfterCompiling(theSUT::orderBy);
//  }
//  @Test 
//  public void testInitHavingAfterCompiling() {
//    initAfterCompiling(theSUT::having);
//  }
//  @Test 
//  public void testInitArgumentsAfterCompiling() {
//    initAfterCompiling(theSUT::withArguments);
//  }
//  @Test 
//  public void testInitColumnsAfterCompiling() {
//    initAfterCompiling(theSUT::columns);
//  }
//  @Test 
//  public void testInitDistinctAfterCompiling() {
//    initAfterCompiling(dummy -> theSUT.distinct());
//  }

  @Test
  public void testExecPlain() {
    DataSource ds = 
      select(getDB(), UserDAO.COLUMNS)
      .from(UserDAO.TABLE_NAME)
      .build();
    DataSet actual = executeQuery(ds);
    DataSet expected = 
        data(ds, getConversion())
        .rows(INITIAL_DATA);
    assertDataSet(expected, actual);
  }

  @Test
  public void testExecWhere() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    DataSource ds = 
     select(getDB(), UserDAO.COLUMNS)
      .from(UserDAO.TABLE_NAME)
      .where("login='" + EXISTING_DATA_ID1 + "'")
      .build();
    DataSet actual = executeQuery(ds);
    DataSet expected = data(ds, getConversion()).row(u);
    assertDataSet(expected, actual);
  }


  @Test
  public void testExecWhereWithArgs() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    DataSource ds = 
      select(getDB(), UserDAO.COLUMNS)
      .from(UserDAO.TABLE_NAME)
      .where("login=?")
      .build(EXISTING_DATA_ID1);
    DataSet actual = 
        executeQuery(ds);     
    DataSet expected = 
        data(ds, getConversion())
        .row(u);
    assertDataSet(expected, actual);
  }
  @Test
  public void testExecColumns1() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    DataSource q =
        select(getDB(), "password")
        .from(UserDAO.TABLE_NAME)
        .where("login=?")
        .build(EXISTING_DATA_ID1);
    DataSet actual = executeQuery(q);
    DataSet expected = 
        data(q)
        .row(u.getPassword());
    assertDataSet(expected, actual);
  }

  @Test
  public void testExecColumns2() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    DataSource q =
        select(getDB(), "password", "name")
        .from(UserDAO.TABLE_NAME)
        .where("login=?")
        .build(EXISTING_DATA_ID1);
    DataSet actual = 
      executeQuery(q);
    DataSet expected = 
        data(q)
        .row(u.getPassword(), u.getName());
    assertDataSet(expected, actual);
  }

  @Test
  public void testExecWithDistinct1() {
    DataSource q =
        select(getDB(), UserDAO.COLUMNS)
        .distinct()
        .from(UserDAO.TABLE_NAME)
        .build();
    DataSet actual = executeQuery(q);
    DataSet expected = 
        data(q, getConversion())
        .rows(INITIAL_DATA);
    assertDataSet(expected, actual);
  }

  @Test
  public void testExecWithDistinct2() {
    DataSource q =
        select(getDB(), "password")
        .distinct()
        .from(UserDAO.TABLE_NAME)
        .build();
    DataSet actual = 
     executeQuery(q);
    HashSet<String> distinctPass = new HashSet<>();
    DataSet expected = data(q);
    for (User u : INITIAL_DATA) {
      if (distinctPass.add(u.getPassword())) {
        expected.row(u.getPassword());
      }
    }
    assertDataSet(expected, actual);
  }

  @Test
  public void testExecWithOrderBy1() {
    DataSource q =
        select(getDB(), UserDAO.COLUMNS)
        .from(UserDAO.TABLE_NAME)
        .orderBy("login")
        .build();
    DataSet actual = executeQuery(q);
    User[] sortedUsers = INITIAL_DATA.clone();
    Arrays.sort(sortedUsers, 
                (a,b) -> a.getLogin().compareTo(b.getLogin()));
    DataSet expected = data(q, getConversion()).rows(sortedUsers);
    assertTrue(expected.sameDataAs(actual));
  }

  @Test
  public void testExecWithOrderBy2() {
    DataSource q =
        select(getDB(), UserDAO.COLUMNS)
        .from(UserDAO.TABLE_NAME)
        .orderBy("password", "login")
        .build();
    DataSet actual = executeQuery(q);
    User[] sortedUsers = INITIAL_DATA.clone();
    Arrays.sort(sortedUsers, 
        (a,b) -> {
          int cmp = a.getPassword().compareTo(b.getPassword());
          if (cmp == 0) {
            cmp = a.getLogin().compareTo(b.getLogin());
          }
          return cmp;
        });
    DataSet expected = 
        data(q, getConversion())
        .rows(sortedUsers);
    assertTrue(expected.sameDataAs(actual));
  }

  <T extends Number> DataSet passCount(DataSource ds, T zero, Function<T,T> incr, BiPredicate<String,T> pred) {
    
    DataSet expected = data(ds);
    HashMap<String,T> count = new HashMap<>();
    for (User u : INITIAL_DATA) {
      count.put(u.getPassword(), 
          incr.apply(count.getOrDefault(u.getPassword(),zero)));
    }
    for (Entry<String,T> e : count.entrySet()) {
      if (pred.test(e.getKey(), e.getValue())) {
        expected.row(e.getKey(), e.getValue());
      }
    }
    return expected;
  }
  
  @Test
  public void testExecWithGroupBy1() {
    DataSource q = select(getDB(), "password","count(login)")
        .from(UserDAO.TABLE_NAME)
        .groupBy("password")
        .build();
    
    DataSet expected = 
        DBCfg.getConfig().doesCountReturnAnInteger() ?
              passCount(q, 0, x -> x + 1, (p,n) -> true) 
            :
              passCount(q, 0L, x -> x + 1L, (p,n) -> true);
    DataSet actual = executeQuery(q);
    assertDataSet(expected, actual);
  }
  
  @Test
  public void testExecWithGroupBy2() {
    DataSource q = select(getDB(), "password","count(login)")
        .from(UserDAO.TABLE_NAME)
        .groupBy("password")
        .having("count(login) > 1")
        .build();
    
    DataSet expected = 
        DBCfg.getConfig().doesCountReturnAnInteger() ?
              passCount(q, 0, x -> x + 1, (p,n) -> n > 1) 
            :
              passCount(q, 0L, x -> x + 1L, (p,n) -> n > 1);
    DataSet actual = executeQuery(q);
    assertDataSet(expected, actual);
  }
}
