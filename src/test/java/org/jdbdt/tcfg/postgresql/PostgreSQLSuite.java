package org.jdbdt.tcfg.postgresql;

import java.io.IOException;
import java.sql.SQLException;

import org.jdbdt.DBEngineTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

@SuppressWarnings("javadoc")
public class PostgreSQLSuite extends DBEngineTestSuite {
  
  private static PostgresProcess process;
  
  @BeforeClass 
  public static void setup() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SQLException { 
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
    final PostgresConfig config = PostgresConfig.defaultWithDbName("test");
    PostgresExecutable exec = runtime.prepare(config);
    process = exec.start();
    // connecting to a running Postgres
    String url = String.format("jdbc:postgresql://%s:%s/%s",
            config.net().host(),
            config.net().port(),
            config.storage().dbName());
    Class.forName("org.postgresql.Driver");
    System.setProperty(DB_URL_PROP, url);
  }
  @AfterClass
  public static void teardown() {
    process.stop();
  }
}
