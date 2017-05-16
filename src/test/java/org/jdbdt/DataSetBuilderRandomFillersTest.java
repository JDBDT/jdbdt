package org.jdbdt;

import static org.junit.Assert.*;
import static org.jdbdt.JDBDT.*;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

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
public class DataSetBuilderRandomFillersTest {

  private static DataSource mockDataSource;
  
  private static final 
  LinkedHashMap<String,Object> BASE_DATA = new LinkedHashMap<>();
     
  private static final int COUNT = 10;

  @BeforeClass
  public static void globalSetup() {
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
    mockDataSource = new MockDataSource(columns);
  }

  @Rule 
  public TestName testName = new TestName();
  DataSetBuilder theSUT;
  DataSet expected;
  String column;
  Random rng;
  
  @Before 
  public void setUp() {
    rng = new Random(Arrays.hashCode(mockDataSource.getColumns()));
    theSUT = builder(mockDataSource);
    for (Map.Entry<String, Object> e : BASE_DATA.entrySet()) {
      theSUT.value(e.getKey(), e.getValue());
    }
  }
  
  interface DataGenerator {
    void next(Map<String,Object> data);
  }

  static DataSet deriveRowSet(int n, DataGenerator dg) {
    DataSet rs = new DataSet(mockDataSource);
    for (int i=0; i < n; i++) {
      @SuppressWarnings("unchecked")
      Map<String,Object> data = (Map<String,Object>) BASE_DATA.clone();
      dg.next(data);
      rs.addRow(new Row(data.values().toArray()));
    }
    return rs;
  }
  
  @Test
  public void testRandomBase() {
    column = "cInteger";
    Function<Random,Integer> customF = r -> r.nextInt(COUNT);
    expected = deriveRowSet(COUNT, data -> {
      data.put(column, rng.nextInt(COUNT));
    });
    theSUT.random(column, customF);
    theSUT.generate(COUNT);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
  
  @Test
  public void testRandomArray() {
    column = "cInteger";
    Integer[] values = new Integer[COUNT];
    for (int i=0; i < COUNT; i++) {
       values[i] = i;
    }
    expected = deriveRowSet(COUNT, data -> {
      data.put(column, values[rng.nextInt(COUNT)]);
    });
    theSUT.random(column, values);
    theSUT.generate(COUNT);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
  @Test
  public void testRandomList() {
    column = "cDouble";
    List<Double> values = new ArrayList<>();
    for (int i=0; i < COUNT; i++) {
       values.add(i * 0.01);
    }
    expected = deriveRowSet(COUNT, data -> {
      data.put(column, values.get(rng.nextInt(COUNT)));
    });
    theSUT.random(column, values);
    theSUT.generate(COUNT);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
  
  @Test
  public void testRandomInt() {
    column = "cInteger";
    expected = deriveRowSet(COUNT, data -> {
      data.put(column, 1 + rng.nextInt(COUNT));
    });
    theSUT.random(column, 1, COUNT);
    theSUT.generate(COUNT);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
  @Test
  public void testRandomLong() {
    column = "cLong";
    expected = deriveRowSet(COUNT, data -> {
      data.put(column, 1 + (rng.nextLong() & Long.MAX_VALUE) % COUNT);
    });    
    theSUT.random(column, 1L, COUNT);
    theSUT.generate(COUNT);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }

  @Test
  public void testRandomFloat() {
    column = "cFloat";
    expected = deriveRowSet(COUNT, data -> {
      data.put(column, 1.0f + rng.nextFloat() * (COUNT-1));
    });
    theSUT.random(column, 1.0f, (float) COUNT);
    theSUT.generate(COUNT);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
  
  @Test
  public void testRandomDouble() {
    column = "cDouble";
    expected = deriveRowSet(COUNT, data -> {
      data.put(column, 1.0 + rng.nextDouble() * (COUNT-1));
    });
    theSUT.random(column, 1.0, COUNT);
    theSUT.generate(COUNT);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }
  
  @Test
  public void testRandomTime() {
    column = "cTime";
    Time min = Time.valueOf("00:00:00");
    Time max = Time.valueOf("23:59:59");
    expected = deriveRowSet(COUNT, data -> {
      data.put(column, new Time(min.getTime() + (rng.nextLong() & Long.MAX_VALUE) % (max.getTime() - min.getTime() + 1)));
    });
    theSUT.random(column, min, max);
    theSUT.generate(COUNT);
    assertTrue(expected.sameDataAs(theSUT.data()));
  }

  @Test(expected=ColumnFillerException.class)
  public void testIfReseedingIsAllowed() {
    theSUT.random("cInteger", rng -> { rng.setSeed(0); return rng.nextInt(); });
    theSUT.generate(1);
  }
  
  static boolean DEBUG = false;
  
  @After
  public void dump() {
    if (DEBUG) {
      System.out.println("> " + testName.getMethodName() + " -- " + column);
      System.out.println("-- expected --");
      for (Row r : expected.getRows()) {
        System.out.println(Arrays.toString(r.data()));
      }
      System.out.println("-- actual --");
      for (Row r : theSUT.data().getRows()) {
        System.out.println(Arrays.toString(r.data()));
      }
    }
  }

}
