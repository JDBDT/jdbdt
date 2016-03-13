package org.jdbdt;

import static org.jdbdt.JDBDT.table;
import static org.junit.Assert.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataBuilderCoreFillerTest extends DBTestCase {

  @Rule public TestName testName = new TestName();

  private static Table table;

  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = table(UserDAO.TABLE_NAME).boundTo(getConnection());
  }

  DataBuilder theSUT;

  static final 
  User BASE_DATA = new User("someLogin", 
                            "someName", 
                            "somePassword",
                            Date.valueOf("2015-12-31"));
  @Before 
  public void setUp() {
    theSUT = new DataBuilder(table);
    //"login", "name", "password", "created" 
    theSUT.value("login", BASE_DATA.getLogin())
          .value("name", BASE_DATA.getName())
          .value("password", BASE_DATA.getPassword())
          .value("created",  dateValue(BASE_DATA.getCreated())); 
  }
  
  interface UserGenerator {
    User next(User base, int index);
  }
  private static void gen(DataSet rs, UserGenerator g, int n) {
    for (int i=0; i < n; i++) {
      User data = g.next(BASE_DATA, i);
      rs.addRow(new RowImpl(getConversion().convert(data)));
    }
  }
  private static DataSet deriveRowSet(int n, UserGenerator ug) {
    DataSet rs = new DataSet();
    gen(rs, ug, n);
    return rs;
  }
  @Test
  public void testSeq1() {
    String userPrefix = "someLogin";
    int count = 10;
    DataSet expected = deriveRowSet(count, (u,i) -> {
      User r = u.clone();
      r.setLogin(userPrefix + i);
      return r;
    });
    theSUT.sequence("login", i -> userPrefix + i);
    theSUT.generate(count);
    assertEquals(expected, theSUT.data());
  }
  @Test
  public void testSeq2() {
    String userPrefix = "someLogin";
    char suffix = '_';
    int count = 10;
    DataSet expected = deriveRowSet(count, (u,i) -> {
      StringBuilder sb = new StringBuilder();
      for (int k = 0; k < i; k++) {
        sb.append(suffix);
      }
      User r = u.clone();
      r.setLogin(userPrefix + sb.toString());
      return r;
    });
    theSUT.sequence("login", userPrefix, s -> s + "_" );
    theSUT.generate(count);
    assertEquals(expected, theSUT.data());
  }
  @Test
  public void testSeq3() {
    String userPrefix = "someLogin";
    int count = 10;
    DataSet expected = deriveRowSet(count, (u,i) -> {
      User r = u.clone();
      r.setLogin(userPrefix + i);
      return r;
    });
    String[] values = new String[count];
    for (int i=0; i < count; i++) {
      values[i] = userPrefix + i;
    }
    theSUT.sequence("login", values);
    theSUT.generate(count);
    assertEquals(expected, theSUT.data());
  }
  @Test
  public void testSeq4() {
    String userPrefix = "someLogin";
    int count = 10;
    DataSet expected = deriveRowSet(count, (u,i) -> {
      User r = u.clone();
      r.setLogin(userPrefix + i);
      return r;
    });
    String[] values = new String[count];
    for (int i=0; i < count; i++) {
      values[i] = userPrefix + i;
    }
    theSUT.sequence("login", Arrays.asList(values));
    theSUT.generate(count);
    assertEquals(expected, theSUT.data());
  }
  @Test
  public void testNullValue() {
    int count = 10;
    DataSet expected = deriveRowSet(2 * count, (u,i) -> {
      User r = u.clone();
      if (i >= 10) {
        r.setLogin(null);
      }
      return r;
    });
    theSUT.generate(count);
    theSUT.nullValue("login");
    theSUT.generate(count);
    assertEquals(expected, theSUT.data());
  }
  @Test
  public void testRemainingColumnsNull() {
    int count = 10;
    DataSet expected = deriveRowSet(count, (u,i) -> {
      User r = u.clone();
      r.setName(null);
      r.setPassword(null);
      r.setCreated(null);
      return r;
    });
    theSUT.reset();
    theSUT.value("login", BASE_DATA.getLogin());
    theSUT.remainingColumnsNull();
    theSUT.generate(count);
    assertEquals(expected, theSUT.data());
  }

}
