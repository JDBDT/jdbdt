package org.jdbdt;

import static org.jdbdt.JDBDT.table;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataBuilderSequenceFillersTest  {

  private static Table table;
  private static final String TABLE_NAME = "Foo";
  
  private static final 
  LinkedHashMap<String,Object> BASE_DATA = new LinkedHashMap<>();
     
  private static final int COUNT = 100;

  @BeforeClass
  public static void globalSetup() throws SQLException {
    BASE_DATA.put("cInteger", -1);
    BASE_DATA.put("cLong", -1L);
    BASE_DATA.put("cBigInteger", BigInteger.valueOf(-1L));
    BASE_DATA.put("cFloat", -1.0f);
    BASE_DATA.put("cDouble", -1);
    BASE_DATA.put("cTimestamp", new Timestamp(0L));
    BASE_DATA.put("cTime", new Time(0L));
    BASE_DATA.put("cDate", new Date(0L));
    String[] columns = new String[BASE_DATA.size()];
    BASE_DATA.keySet().toArray(columns);
    table = table(TABLE_NAME)
           .columns(columns);
  }

  @Rule 
  public TestName testName = new TestName();
  DataBuilder theSUT;
  DataSet expected;
  String column;
  
  @Before 
  public void setUp() {
    theSUT = new DataBuilder(table);
    for (Map.Entry<String, Object> e : BASE_DATA.entrySet()) {
      theSUT.value(e.getKey(), e.getValue());
    }
  }
  
  interface DataGenerator {
     void next(int index, Map<String,Object> data);
  }

  static DataSet deriveRowSet(int n, DataGenerator dg) {
    DataSet rs = new DataSet(table);
    for (int i=0; i < n; i++) {
      @SuppressWarnings("unchecked")
      Map<String,Object> data = (Map<String,Object>) BASE_DATA.clone();
      dg.next(i, data);
      rs.addRow(new RowImpl(data.values().toArray()));
    }
    return rs;
  }
  
  
  @Test
  public void testInteger() {
    column = "cInteger";
    expected = deriveRowSet(COUNT, (i,data) -> {
      data.put(column, i);
    });
    theSUT.sequence(column, 0);
    theSUT.generate(COUNT);
    assertEquals(expected, theSUT.data());
  }
  @Test
  public void testLong() {
    column = "cLong";
    expected = deriveRowSet(COUNT, (i,data) -> {
      data.put(column, (long) i);
    });
    theSUT.sequence(column, 0L);
    theSUT.generate(COUNT);
    assertEquals(expected, theSUT.data());
  }
  
  @Test
  public void testBigInteger() {
    column = "cBigInteger";
    expected = deriveRowSet(COUNT, (i,data) -> {
      data.put(column, BigInteger.ZERO.add(BigInteger.valueOf(i)));
    });
    theSUT.sequence(column, BigInteger.ZERO);
    theSUT.generate(COUNT);
    assertEquals(expected, theSUT.data());
  }
  
  @Test
  public void testFloat() {
    column = "cFloat";
    expected = deriveRowSet(COUNT, (i,data) -> {
      data.put(column, i * 0.5f);
    });
    theSUT.sequence(column, 0.0f, 0.5f);
    theSUT.generate(COUNT);
    assertEquals(expected, theSUT.data());
  }
  
  @Test
  public void testDouble() {
    column = "cDouble";
    expected = deriveRowSet(COUNT, (i,data) -> {
      data.put(column, i * 0.5);
    });
    theSUT.sequence(column, 0.0f, 0.5);
    theSUT.generate(COUNT);
    assertEquals(expected, theSUT.data());
  }

  @Test
  public void testDate() {
    column = "cDate";
    Date start = Date.valueOf("2015-01-01");
    expected = deriveRowSet(COUNT, (i,data) -> {
      data.put(column, new Date(start.getTime() + i * DataBuilder.MILLIS_PER_DAY));
    });
    theSUT.sequence(column, start, 1);
    theSUT.generate(COUNT);
    assertEquals(expected, theSUT.data());
  }
  
  @Test
  public void testTime() {
    column = "cTime";
    Time fixedTime = Time.valueOf("00:00:00");
    int increment = 12321;
    expected = deriveRowSet(COUNT, (i,data) -> {
      data.put(column, new Time(fixedTime.getTime() + i * increment * 1000L));
    });
    theSUT.sequence(column, fixedTime, increment);
    theSUT.generate(COUNT);
    assertEquals(expected, theSUT.data());
  }
  
  @Test
  public void testTimestamp() {
    column = "cTimestamp";
    Timestamp fixedTS = Timestamp.valueOf("2015-01-01 00:00:00.0");
    int increment = 1234321;
    expected = deriveRowSet(COUNT, (i,data) -> {
      data.put(column, new Timestamp(fixedTS.getTime() + i * increment));
    });
    theSUT.sequence(column, fixedTS, increment);
    theSUT.generate(COUNT);
    assertEquals(expected, theSUT.data());
  }
  
  private static boolean DEBUG = false;
  
  @After
  public void dump() {
    if (DEBUG) {
      System.out.println("> " + testName.getMethodName() + " -- " + column);
      System.out.println("-- expected --");
      for (Row r : expected) {
        System.out.println(Arrays.toString(r.getColumnData()));
      }
      System.out.println("-- actual --");
      for (Row r : theSUT.data()) {
        System.out.println(Arrays.toString(r.getColumnData()));
      }
    }
  }

}
