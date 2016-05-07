package org.jdbdt;


import static org.jdbdt.JDBDT.*;
import static org.jdbdt.TestUtil.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.junit.Assert.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryBuilderTest extends DBTestCase { 

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