package org.jdbdt;

import org.jdbdt.tcfg.derby.DerbySuite;
import org.jdbdt.tcfg.h2.H2Suite;
import org.jdbdt.tcfg.hsqlsb.HSQLDBSuite;
import org.jdbdt.tcfg.mysql.MySQLSuite;
import org.jdbdt.tcfg.postgresql.PostgreSQLSuite;
import org.jdbdt.tcfg.sqlite.SQLiteSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({
  AllNonDBTests.class,
  DerbySuite.class,
  HSQLDBSuite.class,
  H2Suite.class,
  MySQLSuite.class,
  PostgreSQLSuite.class,
  SQLiteSuite.class
})
public class AllTests {
  
}
