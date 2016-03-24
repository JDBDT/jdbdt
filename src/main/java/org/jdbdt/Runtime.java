package org.jdbdt;

import java.util.EnumSet;

/**
 * Runtime
 *
 */
final class Runtime {

  /**
   * Singleton instance.
   */
  private static final Runtime INSTANCE = new Runtime(); 
  
  /**
   * Obtain runtime instance.
   * @return The instance in use.
   */
  static final Runtime getRuntime() {
    return INSTANCE;
  }

  /**
   * Trace log.
   */
  Log traceLog; 
  
  /**
   * Trace options.
   */
  EnumSet<Trace> traceOptions;
  
  /**
   * Constructor.
   */
  Runtime() {
    traceLog = new Log(System.err);
    traceOptions = EnumSet.noneOf(Trace.class);
  }
  
  /**
   * Get log in use.
   * @return Log instance in use.
   */
  public Log getLog() {
    return traceLog;
  }

  /**
   * Query if given trace option is enabled.
   * @param option Option.
   * @return <code>true</code> if the option is enabled.
   */
  boolean doLog(Trace option) {
    return traceOptions.contains(option);
  }

  /**
   * Configure trace.
   * @param log Trace log.
   * @param options Trace options.
   */
  void trace(Log log, Trace[] options) {
    traceLog = log;
    traceOptions.clear();
    for (Trace o : options) {
      traceOptions.add(o);
    }
  }

  /**
   * Log data set if a certain trace option is enabled.
   * @param opt Trace option.
   * @param data Data set.
   */
  void logIf(Trace opt, DataSet data) {
    if (doLog(opt)) {
      traceLog.write(data);
    }
  }
  
  /**
   * Log delta if a certain trace option is enabled.
   * @param opt Trace option.
   * @param delta Delta instance.
   */
  void logIf(Trace opt, Delta delta) {
    if (doLog(opt)) {
      traceLog.write(delta);
    }
  }

  /**
   * Log SQL code.
   * @param sql SQL code.
   */
  void logSQL(String sql) {
    if (doLog(Trace.sql)) {
      traceLog.writeSQL(sql);
    }
  }
}
