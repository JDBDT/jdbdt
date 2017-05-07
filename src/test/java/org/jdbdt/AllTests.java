package org.jdbdt;

import org.jdbdt.derby.DerbySuite;
import org.jdbdt.h2.H2Suite;
import org.jdbdt.hsqlsb.HSQLDBSuite;
import org.jdbdt.mysql.MySQLSuite;
import org.jdbdt.postgresql.PostgreSQLSuite;
import org.jdbdt.sqlite.SQLiteSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({
  AllNonDBTests.class,
  DerbySuite.class,
  H2Suite.class,
  HSQLDBSuite.class,
  MySQLSuite.class,
  PostgreSQLSuite.class,
  SQLiteSuite.class
})
public class AllTests {
  
}
