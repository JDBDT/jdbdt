package org.jdbdt;

/**
 * Trace options for use with {@link JDBDT#trace}.
 *
 * @since 0.1
 */
public enum Trace {
  /**
   * Log deltas.
   */
  deltas,
  /**
   * Log deltas upon assertion errors.
   */
  deltasOnError,
  /**
   * Log data set insertions.
   */
  insertions,
  /**
   * Log data set snapshots.
   */
  snapshots,
  /**
   * Log SQL statements.
   */
  sql;
}
