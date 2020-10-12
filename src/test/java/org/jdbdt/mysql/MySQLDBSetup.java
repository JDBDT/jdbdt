/*
 * The MIT License
 *
 * Copyright (c) Eduardo R. B. Marques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

  MySQLDBSetup TravisHandler = new MySQLDBSetup() {

    @Override
    public String start() {
      return "jdbc:mysql://localhost/jdbdt?user=travis&password=&useSSL=false&createDatabaseIfNotExist=true";
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
      case Travis:
        if (System.getenv().getOrDefault("MYSQL_SERVICE", "false").equals("true")) {
          h =  TravisHandler;
          break;
        }
      default:
        h = EmbeddedHandler;
    }
    return h;
  }
}
