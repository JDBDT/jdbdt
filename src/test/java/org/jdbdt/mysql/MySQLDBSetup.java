package org.jdbdt.mysql;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jdbdt.BuildEnvironment;

import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;

@SuppressWarnings("javadoc")
public interface MySQLDBSetup {
  String start();
  void stop();

  MySQLDBSetup AppVeyorHandler = new MySQLDBSetup() {

    @Override
    public String start() {
      return "jdbc:mysql://localhost:3306/jdbdt?user=root&password=Password12!&useSSL=false&createDatabaseIfNotExist=true";
    }
    @Override
    public void stop() { }

  };

  MySQLDBSetup EmbeddedHandler = new MySQLDBSetup() {
    MysqldResource engine;
    static final String DB_PATH = "mysql";
    static final int    DB_PORT = 9999;
    static final String DB_USER = "jdbdt";
    static final String DB_PASS = "jdbdt";

    @Override
    public String start() {
      Map<String,String> args = new HashMap<>();
      args.put(MysqldResourceI.PORT, Integer.toString(DB_PORT));
      args.put(MysqldResourceI.INITIALIZE_USER, "true");
      args.put(MysqldResourceI.INITIALIZE_USER_NAME, DB_USER);
      args.put(MysqldResourceI.INITIALIZE_PASSWORD, DB_PASS);
      engine = new MysqldResource(new File(DB_PATH));
      engine.start("jdbdt-mysqld-thread", args);

      if (!engine.isRunning()) {
        throw new RuntimeException("MySQL did not start.");
      }

      return String.format
          (
              "jdbc:mysql://127.0.0.1:%d/jdbdt?user=%s&password=%s&createDatabaseIfNotExist=true", 
              DB_PORT, 
              DB_USER, 
              DB_PASS
              );
    }

    @Override
    public void stop() {
      engine.shutdown();
    }
  };

  static MySQLDBSetup get() {
    MySQLDBSetup h;
    switch (BuildEnvironment.get()) {
      case AppVeyor:
        h = AppVeyorHandler;
        break;
      default:
        h = EmbeddedHandler;
    }
    return h;
  }

}
