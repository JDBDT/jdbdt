package org.jdbdt.tcfg.mysql;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jdbdt.DBEngineTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;

@SuppressWarnings("javadoc")
public class MySQLSuite extends DBEngineTestSuite {

  @BeforeClass 
  public static void setup() throws ClassNotFoundException {
    startDatabase();
    Class.forName("com.mysql.jdbc.Driver");
    System.setProperty(DB_URL_PROP, 
       "jdbc:mysql://127.0.0.1:" + DB_PORT + 
       "/jdbdt?" +
       "user="+DB_USER + "&" +
       "password=" + DB_PASS + "&createDatabaseIfNotExist=true");
  }
  
  @AfterClass
  public static void teardown() {
    stopDatabase();
  }
  
  private static final File   DB_PATH = new File("mysql");
  private static final int    DB_PORT = 3316;
  private static final String DB_USER = "jdbdt";
  private static final String DB_PASS = "jdbdt";

  private static MysqldResource engine;
  
  private static void startDatabase() {
    engine = new MysqldResource(DB_PATH);
    Map<String,String> args = new HashMap<>();
    args.put(MysqldResourceI.PORT, Integer.toString(DB_PORT));
    args.put(MysqldResourceI.INITIALIZE_USER, "true");
    args.put(MysqldResourceI.INITIALIZE_USER_NAME, DB_USER);
    args.put(MysqldResourceI.INITIALIZE_PASSWORD, DB_PASS);
    engine.start("jdbdt-mysqld-thread", args);
    if (!engine.isRunning()) {
      throw new RuntimeException("MySQL did not start.");
    }
  }
  
  private static void stopDatabase() {
    engine.shutdown();
  }
}
