package org.jdbdt;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Data set builder. 
 * 
 * <p>
 * Objects of this kind provide a convenient manner to 
 * define data sets programmatically.
 * </p>
 * 
 * <p>
 * Data builders can be used as follows: 
 * <ul>
 * <li>
 * They are created with calls to either {@link JDBDT#builder(DataSource)},
 * to create a fresh data set, or to {@link DataSet#build()}
 * to add data to an existing data set.
 * </li>
 * <li>
 * Entries (rows) in the data set are specified by setting one 
 * column filler ({@link ColumnFiller} instance) per each column in the table, 
 * using the {@link #set(String, ColumnFiller)}
 * base method or one of several convenience methods e.g., 
 * the ones available for sequence or pseudo-random values.
 * </li>  
 * <li>
 * Each call to
 * {@link #generate(int)} causes a specified number of rows
 * to be added to the underlying data set, in line with the current
 * column fillers set.
 * </li>
 * <li>
 * The data set instance for a builder can be retrieved using {@link #data()}.
 * </li>
 * </ul>
 * 
 * 
 * @since 0.1 
 * @see JDBDT#builder(DataSource)
 * @see DataSet#build()
 *
 */
public final class DataSetBuilder {

  /**
   * Random number generator seed.
   */
  static final long RNG_SEED = 0xDA7ABA5EL;

  /**
   * Target data set.
   */
  private final DataSet data;
  
  /** 
   * Random number generator.
   */
  private final Random rng;

  /**
   * Map of database column names to column indexes
   */
  private final HashMap<String,Integer> columnIdx = new HashMap<>();

  /**
   * Current settings for fillers.
   */
  private final ColumnFiller<?>[] fillers; 

  /**
   * Number of column fillers set.
   */
  private int fillerCount;

  /**
   * Constructs a new data set builder for the given data source.
   * 
   * <p>
   * The builder will be backed by a fresh data set for the
   * given source.
   * </p>
   * 
   * @param source Data source.
   */
  public DataSetBuilder(DataSource source) {
    this(new DataSet(source));
  }

  /**
   * Constructs a new data set builder backed by an existing data set.
   * 
   * <p>
   * New rows generated using the builder will be added to the given data set.
   * without clearing previously existing rows in it.
   * </p>
   * 
   * @param data Data set associated to builder. 
   */
  public DataSetBuilder(DataSet data) {
    this.data = data;
    String[] columnNames = data.getSource().getColumns();
    fillers = new ColumnFiller[columnNames.length];
    fillerCount = 0;
    for (int idx=0; idx < columnNames.length; idx++) {
      columnIdx.put(columnNames[idx].toLowerCase(), idx);
    }
    // Prevent re-seeding in support of repeatable tests.
    rng = new FixedSeedRandom(Arrays.hashCode(columnNames));
  }

  /**
   * Get number of columns with defined fillers.
   * @return A non-negative integer indicating how many columns 
   *   have an associated column filler.
   */
  int fillerCount() {
    return fillerCount;
  }

  /**
   * Get data set this builder associates to.
   * @return Data set instance.
   */
  public DataSet data() {
    return data; 
  }
  
  /**
   * Associate a number of rows to the data set, according to 
   * the current column fillers' configuration. 
   * 
   * @param count Number of rows (a positive integer)
   * @return The data set instance (for chained calls).
   * @throws ColumnFillerException if there is an error evaluating 
   *     a column filler.
   * @throws InvalidOperationException for an invalid row count, or if
   *     there are columns with no associated fillers.
   */
  public DataSetBuilder generate(int count) {
    ensureValid(count, count > 0);
    if (fillerCount < fillers.length) {
      DataSource source = data.getSource();
      for (int idx = 0; idx < source.getColumnCount(); idx++) {
        if (fillers[idx] == null) {
          throw new InvalidOperationException("No filler is set for column '" + 
              source.getColumns()[idx]);
        }
      }
      throw new InternalErrorException("Filler count does not match fillers set.");
    }
    final String[] colNames = data.getSource().getColumns();
    for (int r=0; r < count; r++) {
      final Object[] colData = new Object[colNames.length];
      for (int c = 0; c < colNames.length; c++) {
        try {
          colData[c] = fillers[c].next();
        }
        catch (Exception e) {
          throw new ColumnFillerException("Error evaluating filler for '" + colNames[c] + "'", e);
        }
      }
      data.addRow(new Row(colData));
    }
    return this;
  }

  /**
   * Set filler for column.
   * @param column Column name.
   * @param filler Column filler. 
   * @return The builder instance (for chained calls).
   */
  public DataSetBuilder set(String column, ColumnFiller<?> filler) {
    ensureArgNotNull(column);
    ensureArgNotNull(filler);
    Integer idx = columnIdx.get(column.toLowerCase());
    if (idx == null) {
      throw new InvalidOperationException("Invalid column name: '" + column + "'.");
    }
    if (fillers[idx] == null) {
      fillerCount ++;
    }
    fillers[idx] = filler;
    return this;
  }

  /**
   * Disable all previous column filler settings.
   * 
   * <p>
   * After a call to this method, no column will have an associated filler.
   * </p>
   */
  public void reset() {
    Arrays.fill(fillers, null);
    fillerCount = 0;
  }

  /**
   * Standard sequence filler.
   * @param <T> Datum type.
   */
  private static class StdSeqFiller<T> implements ColumnFiller<T> {
    /** Next value */
    private T nextValue;
    /** Step function. */
    private final Function<T,T> stepFunction;

    /**
     * Constructor.
     * @param initial Initial value.
     * @param stepFunction Step function.
     */
    StdSeqFiller(T initial, Function<T,T> stepFunction) {
      nextValue = initial;
      this.stepFunction = stepFunction;
    }
    @Override
    public T next() {
      T prev = nextValue;
      nextValue = stepFunction.apply(nextValue);
      return prev;
    }
  }

  /**
   * Constant value filler.
   *
   * @param <T> Datum type.
   * @see DataSetBuilder#value(String, Object)
   * @see DataSetBuilder#nullValue(String)
   */
  private static class ConstantFiller<T> implements ColumnFiller<T> {
    /** Constant value. */
    private final T value;
    /** 
     * Constructor.
     * @param value Value to use
     */
    ConstantFiller(T value) {
      this.value = value;
    }
    @Override
    public T next() {
      return value;
    }
  }

  /** NULL value filler */
  private static final ColumnFiller<?> NULL_FILLER = new ConstantFiller<>(null);

  /**
   * Set the NULL value filler for a column.
   * 
   * @param column Column name.
   * @return The builder instance (for chained calls).
   * @see #value(String, Object)
   */
  public DataSetBuilder nullValue(String column) {
    return set(column, NULL_FILLER);
  }

  /**
   * Set the NULL value filler for all remaining columns. 
   * 
   * <p>
   * A call to this method sets the {@link #nullValue} filler
   * for all columns without an associated filler. 
   * </p>
   * @return  The builder instance (for chained calls).
   * @see #nullValue(String)
   * @see #allColumnsNull()
   */
  public DataSetBuilder remainingColumnsNull() {
    for (int i = 0; i < fillers.length; i++) {
      if (fillers[i] == null) {
        fillers[i] = NULL_FILLER;
        fillerCount++;
      }
    }
    return this;
  }
  
  /**
   * Set the NULL value filler for all columns. 
   * 
   * <p>
   * A call to this method sets the {@link #nullValue} filler
   * for all columns. 
   * </p>
   * @return  The builder instance (for chained calls).
   * @see #nullValue(String)
   * @see #remainingColumnsNull()
   */
  public DataSetBuilder allColumnsNull() {
    Arrays.fill(fillers, NULL_FILLER);
    fillerCount = fillers.length;
    return this;
  }

  /**
   * Set a constant value filler for a column.
   * @param column Column name.
   * @param constant Value of the constant .
   * @return The builder instance (for chained calls).
   */
  public DataSetBuilder value(String column, Object constant) {
    return set(column, new ConstantFiller<Object>(constant)); 
  }
  
  /**
   * Set a constant enum value for a column, converting the enumeration constant to a string.
   * 
   * <p>
   * A call to this method is shorthand for <code>value(column, enumConstant.toString())</code>.
   * </p>
   * 
   * @param <E> Enumeration type.
   * @param column Column name.
   * @param enumConstant Value of the constant.
   * @return The builder instance (for chained calls).
   */
  public <E extends Enum<?>> DataSetBuilder value(String column, E enumConstant) {
    return value(column, enumConstant.toString()); 
  }

  /**
   * Set a sequence filler using a step-function.
   * 
   * <p>
   * The sequence of values generated by the filler starts
   * with the specified initial value, and subsequent values 
   * are generated using the step function which takes as input the previous value.
   * The sequence will then be
   * <code>s(0), s(1), ...</code> where <code>s(0) = initial</code>
   * and <code>s(n+1) = step.apply(s(n))</code> for
   * all <code>n &gt;= 0</code> .
   * </p>
   * 
   * @param <T> Column datum type.
   * @param column Column name.
   * @param initial Initial value.
   * @param step Step function.
   * @return The builder instance (for chained calls).
   * @see #sequence(String, Function)
   */
  public <T> DataSetBuilder sequence(String column, T initial, Function<T,T> step) {
    ensureArgNotNull(initial);
    ensureArgNotNull(step);
    return set(column, new StdSeqFiller<T>(initial, step));
  }

  /**
   * Set sequence filler using a index-based step-function.
   * 
   * <p>
   * A call to this method is equivalent to
   * <code>sequence(column, step, 0)</code>.
   * </p>
   * 
   * @param column Column name.
   * @param step Step function.
   * @return The builder instance (for chained calls).
   * @see #sequence(String, Function, int)
   */
  public DataSetBuilder sequence(String column, Function<Integer,?> step) {
    return sequence(column, step, 0);
  }

  /**
   * Set sequence filler using a index-based step-function.
   * 
   * <p>
   * The sequence of values generated by the filler starts
   * with the specified initial value, and subsequent values 
   * are generated using the step function which takes as input the 
   * index of the row being generated, starting from the initial value.
   * The sequence will then be
   * <code>s(start), s(start+1), ...</code> where <code>s(i) = step.apply(i)</code>
   * for all <code>i &gt;= start</code>.
   * </p>
   * 
   * @param column Column name.
   * @param step Step function.
   * @param initial Initial value fed to step function.
   * @return The builder instance (for chained calls).
   * @see #sequence(String, Object, Function)
   */
  public DataSetBuilder sequence(String column, Function<Integer,?> step, int initial) {
    ensureArgNotNull(step);
    return set(column, new ColumnFiller<Object>() {
      int count = initial;
      @Override
      public Object next() {
        return step.apply(count++);
      }
    });
  }

  /**
   * Set sequence filler using array values.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, i -&gt; values[i % values.length])</code>.
   * </p>
   * @param <T> Type of data.
   * @param column Column name.
   * @param values Sequence of values to use.
   * @return The builder instance (for chained calls).
   * 
   * @see #sequence(String, List)
   * @see #sequence(String,Function)
   * 
   */
  @SafeVarargs
  public final <T> DataSetBuilder sequence(String column, T... values) {
    ensureValidArray(values);
    return sequence(column,  i -> values [i % values.length]);
  }

  /**
   * Set sequence filler using a list of values.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, i -&gt; values.get(i % values.size()))</code>.
   * </p>
   * 
   * @param column Column name.
   * @param values Sequence of values to use.
   * @return The builder instance (for chained calls).
   * 
   * @see #sequence(String,Function)
   */
  public DataSetBuilder sequence(String column, List<?> values) { 
    ensureValidList(values);
    return sequence(column,  i -> values.get(i % values.size()));
  }

  /**
   * Set <code>int</code> value sequence filler for column.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, initial, 1)</code>.
   * </p>
   * 
   * @param column Column name.
   * @param initial Initial sequence value.
   * @return The builder instance (for chained calls).
   * @see #sequence(String, int, int)
   */
  public DataSetBuilder sequence(String column, int initial) {
    return sequence(column, initial, 1);
  }

  /**
   * Set <code>int</code> sequence filler for column
   * with a specified step.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, initial, n -&gt; n + step)</code>.
   * </p>
   * 
   * @param column Column name.
   * @param initial Initial sequence value.
   * @param step Sequence step.
   * @return The builder instance (for chained calls).
   * @see #sequence(String, Object, Function)
   * @see #sequence(String, int)
   */
  public DataSetBuilder sequence(String column, int initial, int step) {
    return sequence(column, (Integer) initial, n -> n + step);
  }

  /**
   * Set <code>long</code> value sequence filler for column.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, initial, 1L)</code>.
   * </p>
   * 
   * @param column Column name.
   * @param initial Initial sequence value.
   * @return The builder instance (for chained calls).
   * @see #sequence(String, Object, Function)
   * @see #sequence(String, long, long)
   */
  public DataSetBuilder sequence(String column, long initial) {
    return sequence(column, initial, 1L);
  }

  /**
   * Set <code>long</code> sequence filler for column
   * with a specified step.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, initial, n -&gt; n + step)</code>.
   * </p>
   * 
   * @param column Column name.
   * @param initial Initial sequence value.
   * @param step Sequence step.
   * @return The builder instance (for chained calls).
   * @see #sequence(String, Object, Function)
   * @see #sequence(String, long)
   */
  public DataSetBuilder sequence(String column, long initial, long step) {
    return sequence(column, (Long) initial, n -> n + step);
  }

  /**
   * Set {@link BigInteger} sequence filler for column.
   * 
   * <p>
   * A call to this method is shorthand for
   * <code>sequence(column, initial, BigInteger.ONE)</code>.
   * </p>
   * 
   * @param column Column name.
   * @param initial Initial sequence value.
   * @return The builder instance (for chained calls).
   * @see #sequence(String, Object, Function)
   * @see #sequence(String, BigInteger, BigInteger)
   */
  public DataSetBuilder sequence(String column, BigInteger initial) {
    ensureArgNotNull(initial);
    return sequence(column, initial, BigInteger.ONE);
  }

  /**
   * Set {@link BigInteger} sequence filler for column
   * with a specified step.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, initial, n -&gt; n.add(step))</code>.
   * </p>
   * 
   * @param column Column name.
   * @param initial Initial sequence value.
   * @param step Sequence step.
   * @return The builder instance (for chained calls).
   * @see #sequence(String, Object, Function)
   * @see #sequence(String, BigInteger)
   */
  public DataSetBuilder sequence(String column, BigInteger initial, BigInteger step) {
    ensureArgNotNull(initial);
    ensureArgNotNull(step);
    return sequence(column, initial, n -> n.add(step));
  }

  /**
   * Set <code>float</code> sequence filler for column
   * with a specified step.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, initial, x -&gt; x + step)</code>.
   * </p>
   * 
   * @param column Column name.
   * @param initial Initial sequence value.
   * @param step Sequence step.
   * @return The builder instance (for chained calls).
   * @see #sequence(String, Object, Function)
   * @see #sequence(String, double, double)
   */
  public DataSetBuilder sequence(String column, float initial, float step) {
    return sequence(column, (Float) initial, x -> x + step);
  }
  /**
   * Set <code>double</code> sequence filler for column
   * with a specified step.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, initial, x -&gt; x + step)</code>.
   * </p>
   * 
   * @param column Column name.
   * @param initial Initial sequence value.
   * @param step Sequence step.
   * @return The builder instance (for chained calls).
   * @see #sequence(String, Object, Function)
   * @see #sequence(String, float, float)
   */
  public DataSetBuilder sequence(String column, double initial, double step) {
    return sequence(column, (Double) initial, x -> x + step);
  }

  /**
   * Milliseconds per day.
   */
  public static final long MILLIS_PER_DAY = 86400000L;

  /**
   * Set {@link Date} sequence filler for column
   * with a specified step in days.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, initial, d -&gt; new Date(x.getTime() + step * MILLIS_PER_DAY))</code>.
   * </p>
   * 
   * @param column Column day.
   * @param initial Initial date.
   * @param step Step in days.
   * @return The builder instance (for chained calls).
   * 
   * @see #sequence(String, Time, int)
   * @see #sequence(String, Timestamp, long)
   * @see #sequence(String, Object, Function)
   */
  public DataSetBuilder sequence(String column, Date initial, int step) {
    ensureArgNotNull(initial);
    return sequence(column, initial, d -> new Date(d.getTime() + step * MILLIS_PER_DAY));
  }

  /**
   * Set {@link Time} sequence filler for column
   * with a specified step in seconds.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, initial, t -&gt; new Time(t.getTime() + step * 1000L))</code> .
   * </p>
   * 
   * @param column Column day.
   * @param initial Initial date.
   * @param step Step in seconds.
   * @return The builder instance (for chained calls).
   * 
   * @see #sequence(String, Date, int)
   * @see #sequence(String, Timestamp, long)
   * @see #sequence(String, Object, Function)
   */
  public DataSetBuilder sequence(String column, Time initial, int step) {
    ensureArgNotNull(initial);
    return sequence(column, initial, t -> new Time(t.getTime() + step * 1000L));
  }

  /**
   * Set {@link Timestamp} sequence filler for column
   * with a specified step in milliseconds.
   * 
   * <p>
   * A call to this method is shorthand for 
   * <code>sequence(column, initial, ts -&gt; new Timestamp(ts.getTime() + step))</code>.
   * </p>
   * 
   * @param column Column day.
   * @param initial Initial date.
   * @param step Step in milliseconds.
   * @return The builder instance (for chained calls).
   * 
   * @see #sequence(String, Date, int)
   * @see #sequence(String, Time, int)
   * @see #sequence(String, Object, Function)
   */
  public DataSetBuilder sequence(String column, Timestamp initial, long step) {
    ensureArgNotNull(initial);
    return sequence(column, initial, ts -> new Timestamp(ts.getTime() + step));
  }

  /**
   * Set random filler for column using an array of values.
   * 
   * <p>
   * The specified column will be filled with values that 
   * are uniformly sampled from the given array. 
   * </p>
   * 
   * @param <T> Type of data.
   * @param column Column name.
   * @param values  Values to use.
   * @return The builder instance (for chained calls).
   * 
   * @see #random(String, List)
   * @see #sequence(String, Object...)
   */
  @SafeVarargs
  public final <T> DataSetBuilder random(String column, T... values) {
    ensureValidArray(values);
    return set(column, () -> values[rng.nextInt(values.length)]);
  }

  /**
   * Set random filler for column using a list of values.
   * 
   * <p>
   * The specified column will be filled with values that 
   * are uniformly sampled from the given list. 
   * </p>
   * 
   * @param column Column name.
   * @param values  Values to use.
   * @return The builder instance (for chained calls).
   * 
   * @see #random(String, Object...)
   * @see #sequence(String, List)
   */
  public DataSetBuilder random(String column, List<?> values) {
    ensureValidList(values);
    return set(column,() -> values.get(rng.nextInt(values.size())));
  }

  /**
   * Set random filler using <code>int</code> values.
   * 
   * <p>
   * The specified column will be filled with values that 
   * are uniformly sampled from the interval <code>[min,max]</code>.
   * </p>
   * 
   * @param column Column name.
   * @param min Minimum value.
   * @param max Maximum value.
   * @return The builder instance (for chained calls).
   * 
   * @see #random(String, long, long)
   * @see #random(String, float, float)
   * @see #random(String, double, double)
   */
  public  DataSetBuilder random(String column, int min, int max) {
    ensureValidRange(min, max);
    int n = max - min + 1;
    return set(column, () -> min + rng.nextInt(n));
  }

  /**
   * Set random filler using <code>long</code> values.
   * 
   * <p>
   * The specified column will be filled with values that 
   * are uniformly sampled from the interval <code>[min,max]</code>.
   * </p>
   * 
   * @param column Column name.
   * @param min Minimum value.
   * @param max Maximum value.
   * @return The builder instance (for chained calls).
   * 
   * @see #random(String, int, int)
   * @see #random(String, float, float)
   * @see #random(String, double, double)
   */
  public  DataSetBuilder random(String column, long min, long max) {
    ensureValidRange(min, max);
    return set(column, () ->  nextRandomLong(min, max));
  }

  /**
   * Set random filler using <code>float</code> values.
   * 
   * <p>
   * The specified column will be filled with values that 
   * are uniformly sampled from the interval <code>[min,max]</code>.
   * </p>
   * 
   * @param column Column name.
   * @param min Minimum value.
   * @param max Maximum value.
   * @return The builder instance (for chained calls).
   * 
   * @see #random(String, int, int)
   * @see #random(String, long, long)
   * @see #random(String, double, double)
   */
  public  DataSetBuilder random(String column, float min, float max) {
    ensureValidRange(min, max);
    float diff = max - min;
    return set(column, () -> min + rng.nextFloat() * diff);
  }

  /**
   * Set random filler using <code>double</code> values.
   * 
   * <p>
   * The specified column will be filled with values that 
   * are uniformly sampled from the interval <code>[min,max]</code>.
   * </p>
   * 
   * @param column Column name.
   * @param min Minimum value.
   * @param max Maximum value.
   * @return The builder instance (for chained calls).
   * 
   * @see #random(String, int, int)
   * @see #random(String, long, long)
   * @see #random(String, float, float)
   */
  public  DataSetBuilder random(String column, double min, double max) {
    ensureValidRange(min, max);
    double diff = max - min;
    return set(column, () -> min + rng.nextDouble() * diff);
  }

  /**
   * Set random filler using {@link Date} values.
   * 
   * <p>
   * The specified column will be filled with values that 
   * are uniformly sampled from the interval <code>[min,max]</code>.
   * </p>
   * 
   * @param column Column name.
   * @param min Minimum value.
   * @param max Maximum value.
   * @return The builder instance (for chained calls).
   * 
   * @see #random(String, Time, Time)
   * @see #random(String, Timestamp, Timestamp)
   * 
   */
  public  DataSetBuilder random(String column, Date min, Date max) {
    ensureValidRange(min, max);
    long a = min.getTime() / MILLIS_PER_DAY;
    long b = max.getTime() / MILLIS_PER_DAY ;
    return set(column, () -> new Date(nextRandomLong(a, b) * MILLIS_PER_DAY));
  }

  /**
   * Auxiliary method that generates a pseudo-random long
   * within a given internal
   * @param a Lower bound
   * @param b Upper bound
   * @return A long value in the interval <code>[a, b]</code>
   */
  private long nextRandomLong(long a, long b) {
    return a + (rng.nextLong() & Long.MAX_VALUE) % (b - a + 1);
  }

  /**
   * Set random filler using {@link Time} values.
   * 
   * <p>
   * The specified column will be filled with values that 
   * are uniformly sampled from the interval <code>[min,max]</code>.
   * </p>
   * 
   * @param column Column name.
   * @param min Minimum value.
   * @param max Maximum value.
   * @return The builder instance (for chained calls).
   * 
   * @see #random(String, Timestamp, Timestamp)
   * @see #random(String, Date, Date)
   */
  public  DataSetBuilder random(String column, Time min, Time max) {
    ensureValidRange(min, max);
    final long a = min.getTime();
    final long b = max.getTime();
    return set(column, () -> new Time(nextRandomLong( a, b)));
  }

  /**
   * Nanoseconds per second.
   */
  private static final int NANO_PER_SEC = 1_000_000_000; 

  /**
   * Nano seconds per millisecond.
   */
  private static final int NANO_PER_MSEC = 1_000_000; 


  /**
   * Set random filler using {@link Timestamp} values.
   * 
   * <p>
   * The specified column will be filled with values that 
   * are uniformly sampled from the interval <code>[min,max]</code>.
   * </p>
   * 
   * @param column Column name.
   * @param min Minimum value.
   * @param max Maximum value.
   * @return The builder instance (for chained calls).
   * 
   * @see #random(String, Time, Time)
   * @see #random(String, Date, Date)
   */
  public  DataSetBuilder random(String column, Timestamp min, Timestamp max) {
    ensureValidRange(min, max);
    final long a = min.getTime() * NANO_PER_MSEC  + min.getNanos(); 
    final long n = max.getTime() * NANO_PER_MSEC + max.getNanos() - a + 1;
    return set(column, 
        () -> {
          long v = a + rng.nextLong() % n;
          Timestamp ts = new Timestamp(v / NANO_PER_SEC);
          ts.setNanos((int) (v % NANO_PER_SEC));
          return ts;
        }
        );
  }

  /**
   * Set customized random filler.
   * 
   * <p>
   * The specified column will be filled with values that are obtained
   * from a generator function. The generator function
   * takes a {@link Random} instance as an argument and returns a value: 
   * it should use the generator to produce column values in deterministic
   * fashion (in particular, the random number generator argument should not 
   * be re-seeded).
   * </p>
   * 
   * <p><b>Illustration of use</b></p>
   * <p>The filler below will yield strings
   * <code>"ID_0", ..., "ID_9"</code> with an uniform
   * distribution:</p>
   * <blockquote><pre>
   * DataSet ds = ...;
   * ...
   * ds.random("SomeColumn", rng -&gt; "ID_" + rng.nextInt(10));
   * </pre></blockquote>
   * @param column Column name.
   * @param gen Generator function.
   *
   * @return The builder instance (for chained calls).
   *
   * @see java.util.Random
   * @see #random(String, int, int)
   * @see #random(String, long, long)
   * @see #random(String, float, float)
   * @see #random(String, double, double)
   * @see #random(String, Date, Date)
   * @see #random(String, Time, Time)
   * @see #random(String, Timestamp, Timestamp)
   */
  public DataSetBuilder random(String column, Function<Random,?> gen) {
    return set(column, () -> gen.apply(rng));
  }

  /**
   * Non-null validation utility method.
   * @param o Object reference.
   * @throws InvalidOperationException if <code>o == null</code>.
   */
  private static void ensureArgNotNull(Object o)  {
    if (o == null) {
      throw new InvalidOperationException("Null argument.");
    }
  }

  /**
   * Array validation utility method.
   * @param array Array reference.
   * @throws InvalidOperationException if the array is <code>null</code> or empty.
   */
  private static void ensureValidArray(Object[] array) {
    if (array == null) {
      throw new InvalidOperationException("Null array argument.");
    }
    if (array.length == 0) {
      throw new InvalidOperationException("Empty array argument.");

    }
  }

  /**
   * List validation utility method.
   * @param list Array reference.
   * @throws InvalidOperationException if the list is <code>null</code> or empty.
   */
  private static void ensureValidList(List<?> list) {
    if (list == null) {
      throw new InvalidOperationException("Null list argument.");
    }
    if (list.isEmpty()) {
      throw new InvalidOperationException("Empty list argument.");
    }
  }
  /**
   * Condition validation utility method.
   * @param o Object reference.
   * @param condition Boolean value for condition.
   * @throws InvalidOperationException if <code>condition == false</code>.
   */
  private static void ensureValid(Object o, boolean condition) {
    if (! condition) {
      throw new InvalidOperationException("Invalid value for parameter: " + o);
    }
  }

  /**
   * Range validation utility method.
   * @param <T> Type of data.
   * @param min Minimum value.
   * @param max Maximum value.
   * @throws InvalidOperationException if the range is not valid.
   */
  private static <T extends Comparable<T>> void ensureValidRange(T min, T max) {
    if (min == null) {
      throw new InvalidOperationException("Null value for minimum.");
    }
    if (max == null) {
      throw new InvalidOperationException("Null value for maximum.");
    }
    if (min.compareTo(max) >= 0) {
      throw new InvalidOperationException("Invalid range: " + min + " >= " + max);
    }
  }

  /**
   * A subclass of {@link java.util.Random} that inhibits re-seeding.
   * 
   * <p>
   * The only difference to {@link java.util.Random} is that a call to {@link #setSeed(long)} 
   * will result in an {@link InvalidOperationException} being thrown.
   * The seed can only be specified once at construction time.
   * </p>
   * 
   */
  private static final class FixedSeedRandom extends Random {
    
    /**
     * Flag indicating if seed was set.
     */
    private boolean seedIsSet;
    
    /**
     * Constructor.
     * @param seed Initial seed.
     */
    FixedSeedRandom(long seed) {
      super(seed);
    }
    
    /**
     * Always throws {@link InvalidOperationException} when called, 
     * overriding the behavior of {@link java.util.Random}.
     * 
     * @throws InvalidOperationException in all cases when called.
     */
    @Override
    public void setSeed(long seed) {
      if (!seedIsSet) {
        super.setSeed(seed); // called from the constructor of java.util.Random
        seedIsSet = true;
      } else {
        throw new ColumnFillerException("For repeatable tests do not reset the seed.");
      }
    }
    
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
  }
}
