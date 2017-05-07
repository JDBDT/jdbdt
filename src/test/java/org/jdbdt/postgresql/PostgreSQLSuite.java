package org.jdbdt.postgresql;

import java.io.IOException;

import org.jdbdt.DBConfig;
import org.jdbdt.DBEngineTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

@SuppressWarnings("javadoc")
public class PostgreSQLSuite extends DBEngineTestSuite {
  
  @BeforeClass 
  public static void setup() throws ClassNotFoundException, IOException { 
    DBConfig.getConfig()
      .reset()
      .setDriver("org.postgresql.Driver")
      .setURL(startDatabase());
  }
  
  @AfterClass
  public static void teardown() {
    stopDatabase();
  }
  
  private static PostgresProcess process;

  private static String startDatabase() throws IOException {
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
    final PostgresConfig config = PostgresConfig.defaultWithDbName("jdbdt-postgresql-test");
    PostgresExecutable exec = runtime.prepare(config);
    process = exec.start();
    return String.format("jdbc:postgresql://%s:%s/%s",
            config.net().host(),
            config.net().port(),
            config.storage().dbName());
  }
  
  private static void stopDatabase() {
    process.stop();
  }
}
