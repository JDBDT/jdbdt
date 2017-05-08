package org.jdbdt.postgresql;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.jdbdt.DBConfig;
import org.jdbdt.DBEngineTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.distribution.Version;


@SuppressWarnings("javadoc")
public class PostgreSQLSuite extends DBEngineTestSuite {
  private static EmbeddedPostgres postgres;

  @BeforeClass 
  public static void setup() throws ClassNotFoundException, IOException { 
    postgres = new EmbeddedPostgres(Version.V9_6_2);
    Path cachePath = FileSystems.getDefault().getPath(System.getProperty("user.home") + "/.embedpostgresql/");
    String url = postgres.start(EmbeddedPostgres.cachedRuntimeConfig(cachePath));
    
    DBConfig.getConfig()
    .reset()
    .setDriver("org.postgresql.Driver")
    .setURL(url);
  }

  @AfterClass
  public static void teardown() {
    postgres.stop();
  }
}
