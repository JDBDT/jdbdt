package org.jdbdt;


import static org.jdbdt.JDBDT.*;

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
  public void createQuery() throws SQLException {
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
  
  <T> void qset(S s, QMutator<T> m, T arg) {
    qsetup.put(s, arg);
    assertSame(theSUT, m.set(arg));
  }

  void qverify() {
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
    qverify();
  }
  
  @Test
  public void testInitWhere() {
    qset(S.WHERE, theSUT::where, "login='foo'");
    qverify();
  }
  
  @Test
  public void testInitOrderBy1() {
    qset(S.ORDER_BY, theSUT::orderBy, new String[] { "login" });
    qverify();
  }
  
  @Test
  public void testInitOrderBy2() {
    qset(S.ORDER_BY, theSUT::orderBy, new String[] { "password", "login" });
    qverify();
  }
  
  
  @Test
  public void testInitGroupBy1() {
    qset(S.GROUP_BY, theSUT::groupBy, new String[] { "password" });
    qverify();
  }
  
  @Test
  public void testInitGroupBy2() {
    // TODO sensible query
    qset(S.GROUP_BY, theSUT::groupBy, new String[] { "password", "login" });
    qverify();
  }
  
  @Test
  public void testInitHaving() {
    // TODO sensible query
    qset(S.HAVING, theSUT::having, "created NOT NULL");
    qverify();
  }
  @Test
  public void testInitQueryArguments() {
    qset(S.ARGS, theSUT::withArguments, new Object[] { "foo", 1 });
    qverify();
  }
  
  @Test
  public void testInitChain1() {
    qset(S.WHERE, theSUT::where, "login LIKE '%user%'");
    qset(S.ORDER_BY, theSUT::orderBy, new String[] { "login" });
    qverify();
  }
  
  
  @Test
  public void testInitChain2() {
    // TODO sensible query
    qset(S.WHERE, theSUT::where, "login LIKE ?");
    qset(S.ARGS, theSUT::withArguments, new Object[] { "foo%" });
    qset(S.ORDER_BY, theSUT::orderBy, new String[] { "login" });
    qset(S.HAVING, theSUT::having, "created NOT NULL");
    qverify();
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
  @Test 
  public void testInitDistinctTwice() {
    initTwice(dummy -> theSUT.distinct());
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
  @Test 
  public void testInitDistinctAfterCompiling() {
    initAfterCompiling(dummy -> theSUT.distinct());
  }
  
  void matchDataSets(DataSet expected, DataSet actual) {
    actual.enforceHOrdering();
    expected.enforceHOrdering();
    assertEquals(actual, expected);
  }
  @Test
  public void testExecPlain() {
    DataSet actual = theSUT.executeQuery(false);
    DataSet expected = 
      data(theSUT, getConversion())
        .rows(INITIAL_DATA);
    matchDataSets(actual, expected);
  }
  
  @Test
  public void testExecWhere() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    DataSet actual = theSUT
                    .where("login='" + EXISTING_DATA_ID1 + "'")
                    .executeQuery(false);
    DataSet expected = 
        data(theSUT, getConversion())
          .row(u);
    matchDataSets(actual, expected);
  }
  
  
  @Test
  public void testExecWhereWithArgs() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    assertNotNull(u);
    DataSet actual = theSUT
                    .where("login=?")
                    .withArguments(EXISTING_DATA_ID1)
                    .executeQuery(false);
    DataSet expected = 
        data(theSUT, getConversion())
          .row(u);
    matchDataSets(actual, expected);
  }
  @Test
  public void testExecColumns1() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    DataSet actual = theSUT
                    .columns("password")
                    .where("login=?")
                    .withArguments(EXISTING_DATA_ID1)
                    .executeQuery(false);
    DataSet expected = 
        data(theSUT)
          .row(u.getPassword());
    matchDataSets(actual, expected);
  }
  
  @Test
  public void testExecColumns2() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    DataSet actual = theSUT
                    .columns("password","name")
                    .where("login=?")
                    .withArguments(EXISTING_DATA_ID1)
                    .executeQuery(false);
    DataSet expected = 
        data(theSUT)
          .row(u.getPassword(), u.getName());
    matchDataSets(actual, expected);
  }
  
  @Test
  public void testExecWithDistinct1() {
    DataSet actual = theSUT
                    .distinct()
                    .executeQuery(false);
    DataSet expected = 
      data(theSUT, getConversion())
        .rows(INITIAL_DATA);
    matchDataSets(actual, expected);
  }
  
  @Test
  public void testExecWithDistinct2() {
    DataSet actual = theSUT
                    .distinct()
                    .columns("password")
                    .executeQuery(false);
    HashSet<String> distinctPass = new HashSet<>();
    DataSet expected = data(theSUT);
    for (User u : INITIAL_DATA) {
      if (distinctPass.add(u.getPassword())) {
        expected.row(u.getPassword());
      }
    }
    matchDataSets(actual, expected);
  }
  
  @Test
  public void testExecWithOrderBy1() {
    DataSet actual = theSUT
                    .orderBy("login")
                    .executeQuery(false);
    User[] sortedUsers = INITIAL_DATA.clone();
       Arrays.sort(sortedUsers, 
                   (a,b) -> 
                     a.getLogin().compareTo(b.getLogin()));
    DataSet expected = 
      data(theSUT, getConversion())
        .rows(sortedUsers);
    assertEquals(actual, expected);
  }
  
  @Test
  public void testExecWithOrderBy2() {
    DataSet actual = theSUT
                    .orderBy("password", "login")
                    .executeQuery(false);
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
      data(theSUT, getConversion())
        .rows(sortedUsers);
    assertEquals(actual, expected);
  }
  
  <T extends Number> DataSet passCount(T zero, Function<T,T> incr, BiPredicate<String,T> pred) {
    DataSet expected = data(theSUT);
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
    DataSet actual = theSUT
        .columns("password","count(login)")
        .groupBy("password")
        .executeQuery(false);
    DataSet expected = 
      DBCfg.getConfig().doesCountReturnAnInteger() ?
        passCount(0, x -> x + 1, (p,n) -> true) 
      :
        passCount(0L, x -> x + 1L, (p,n) -> true);
    matchDataSets(actual, expected);
  }
  @Test
  public void testExecWithGroupBy2() {
    DataSet actual = theSUT
        .columns("password","count(login)")
        .groupBy("password")
        .having("count(login) > 1")
        .executeQuery(false);
    DataSet expected = 
      DBCfg.getConfig().doesCountReturnAnInteger() ?
        passCount(0, x -> x + 1, (s,n) -> n > 1) 
      :
        passCount(0L, x -> x + 1L, (s,n) -> n > 1L);
    matchDataSets(actual, expected);
  }
}