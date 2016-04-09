package org.jdbdt;

import static org.junit.Assert.*;


import java.sql.SQLException;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

@SuppressWarnings("javadoc")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataSetBuilderCoreTest extends DBTestCase {

  @Rule public TestName testName = new TestName();

  private static Table table;

  private static final ColumnFiller<?> DUMMY_FILLER
  = new ColumnFiller<Object>() {
    @Override
    public Object next() {
      return null;
    }
  };
  @BeforeClass
  public static void globalSetup() throws SQLException {
    table = getDB().table(UserDAO.TABLE_NAME).columns(UserDAO.COLUMNS);
  }

  DataSetBuilder theSUT;

  @Before 
  public void setUp() {
    theSUT = new DataSetBuilder(table);
  }

  private static void assertEmptyDataSet(DataSetBuilder sut) {
    assertEquals("filler count", 0, sut.fillerCount());
    assertEquals("row count", 0, sut.size());
    assertEquals("empty row list", 0, sut.data().size());
  }

  @Test
  public void testConstruction() {
    assertEmptyDataSet(theSUT);
  }
  
  private static void 
  invalidUse
  (DataSetBuilder sut, Consumer<DataSetBuilder> operation, Consumer<DataSetBuilder> assertions) {
    try {
      operation.accept(sut);
      fail("Expected " + InvalidUsageException.class);
    }
    catch (InvalidUsageException e) {
      assertions.accept(sut);
    }
  }


  private void invalidFiller(String c, ColumnFiller<?> f) {
    invalidUse(theSUT,
               sut -> sut.set(c,f),
               DataSetBuilderCoreTest::assertEmptyDataSet
               );
  }
  @Test
  public void testInvalidFiller1() {
    invalidFiller("InvalidField",  DUMMY_FILLER);
  }
  @Test
  public void testInvalidFiller2() {
    invalidFiller("login",  null);
  }
  @Test
  public void testInvalidFiller3() {
    invalidFiller(null,  DUMMY_FILLER);
  }
  @Test
  public void testValidFiller() {
    theSUT.value("login", "root");
    assertEquals("fillers set", 1, theSUT.fillerCount());
  }
  @Test
  public void testFillerReset() {
    theSUT.value("login", "root");
    theSUT.reset();
    assertEquals("fillers set", 0, theSUT.fillerCount());
  }
  @Test
  public void testAllFillersSet() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    Object[] row = getConversion().convert(u);
    for (int c = 0; c < row.length; c++) {
      theSUT.value(UserDAO.COLUMNS[c], row[c]);
    }
    assertEquals("fillers set", UserDAO.COLUMNS.length, theSUT.fillerCount());
    assertEquals("row count", 0, theSUT.size());
    assertEquals("no rows", 0, theSUT.data().size());
  }
  private void checkMissingFillers(int N) {
    invalidUse
    (theSUT,
     sut -> sut.generate(1),
     sut -> {
       assertEquals("fillers set", N, sut.fillerCount());
       assertEquals("row count", 0, sut.size());
       assertEquals("no rows", 0, sut.data().size());
     });
  }
  @Test
  public void testMissingFillers0() throws SQLException {
    checkMissingFillers(0);
  }
  @Test
  public void testMissingFillers1() throws SQLException {
    theSUT.value("login", "root");
    checkMissingFillers(1);
  }
  @Test
  public void testMissingFillersAllButOne() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    Object[] row = getConversion().convert(u);
    for (int c = 0; c < row.length-1; c++) {
      theSUT.value(UserDAO.COLUMNS[c], row[c]);
    }
    checkMissingFillers(row.length-1);
  }
  @Test
  public void testGenerate0() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    Object[] row = getConversion().convert(u);
    for (int c = 0; c < row.length; c++) {
      theSUT.value(UserDAO.COLUMNS[c], row[c]);
    }
    invalidUse
    (theSUT,
     sut -> sut.generate(0),
     sut -> {
       assertEquals("fillers set", UserDAO.COLUMNS.length, sut.fillerCount());
       assertEquals("row count", 0, sut.size());
       assertEquals("no rows", 0, sut.data().size());
    });
  }
  
  @Test
  public void testGenerateMinus1() throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    Object[] row = getConversion().convert(u);
    for (int c = 0; c < row.length; c++) {
      theSUT.value(UserDAO.COLUMNS[c], row[c]);
    }
    invalidUse
    (theSUT,
     sut -> sut.generate(-1),
     sut -> {
       assertEquals("fillers set", UserDAO.COLUMNS.length, sut.fillerCount());
       assertEquals("row count", 0, sut.size());
       assertEquals("no rows", 0, sut.data().size());
    });
  }
  
  private void testSimpleGeneration(final int N) throws SQLException {
    User u = getDAO().query(EXISTING_DATA_ID1);
    Object[] rowData = getConversion().convert(u);
    DataSet expectedRows = new DataSet(table);
    for (int i=0; i < N; i++) {
      expectedRows.addRow(new RowImpl(rowData));
    }
    for (int col = 0; col < rowData.length; col++) {
      theSUT.value(UserDAO.COLUMNS[col], rowData[col]);
    }
    theSUT.generate(N);
    assertEquals("fillers set", UserDAO.COLUMNS.length, theSUT.fillerCount());
    assertEquals("row count", N, theSUT.size());
    assertEquals(expectedRows, theSUT.data());
  }
  @Test
  public void testGenerateOneRow() throws SQLException {
    testSimpleGeneration(1);
  }

  @Test
  public void testGenerate100Rows() throws SQLException {
    testSimpleGeneration(100);
  }
  @Test
  public void testCaching1() throws SQLException {
    testSimpleGeneration(2);
    DataSet r1 = theSUT.data();
    DataSet r2 = theSUT.data();
    assertSame(r1, r2);
  }
  @Test
  public void testCaching2() throws SQLException {
    testSimpleGeneration(2);
    DataSet r1 = theSUT.data();
    theSUT.reset();
    DataSet r2 = theSUT.data();
    assertSame(r1, r2);
  }
  
  @Test
  public void testCaching3() throws SQLException {
    testSimpleGeneration(1);
    DataSet r1 = theSUT.data();
    theSUT.generate(1);
    DataSet r2 = theSUT.data();
    assertNotSame(r1, r2);
  }
}
