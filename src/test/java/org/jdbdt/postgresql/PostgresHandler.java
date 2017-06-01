package org.jdbdt.postgresql;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.distribution.Version;

@SuppressWarnings("javadoc")
public enum PostgresHandler {
  AppVeyor {
    @Override
    String start() {
      return "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=Password12!";
    }
    @Override
    void stop() { }
  },
  Embedded {
    EmbeddedPostgres postgres;

    @Override
    String start() {
      postgres = new EmbeddedPostgres(Version.V9_6_2);
      Path cachePath = FileSystems.getDefault().getPath(System.getProperty("user.home") + "/.embedpostgresql/");
      try {
        return postgres.start(EmbeddedPostgres.cachedRuntimeConfig(cachePath));
      } catch (Exception e) {
        throw new InternalError(e);
      }
    }

    @Override
    void stop() {
      postgres.stop();
    }

  };

  static PostgresHandler getInstance() {
    String buildEnv = System.getenv("BUILD_ENVIRONMENT");
    PostgresHandler handler = Embedded; // by default
    if (buildEnv != null) {
      try {
        handler = Enum.valueOf(PostgresHandler.class, buildEnv);
      } 
      catch (IllegalArgumentException e) { 
        // stick with default
      }
    }
    return handler;
  }

  abstract String start();
  abstract void stop();
}
