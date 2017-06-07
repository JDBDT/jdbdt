package org.jdbdt.postgresql;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.jdbdt.BuildEnvironment;

import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.distribution.Version;

@SuppressWarnings("javadoc")
public interface PostgreSQLSetup {
  String start();
  void stop();

  PostgreSQLSetup AppVeyorHandler = new PostgreSQLSetup() {
    @Override
    public String start() {
      return "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=Password12!";
    }
    @Override
    public void stop() { }

  };
  
  PostgreSQLSetup TravisHandler = new PostgreSQLSetup() {
    @Override
    public String start() {
      return "jdbc:postgresql://localhost/jdbdt?user=postgres&password=";
    }
    @Override
    public void stop() { }
  };
  
  PostgreSQLSetup EmbeddedHandler = new PostgreSQLSetup() {
    EmbeddedPostgres postgres;

    @Override
    public String start() {
      postgres = new EmbeddedPostgres(Version.V9_6_2);
      Path cachePath = FileSystems.getDefault().getPath(System.getProperty("user.home") + "/.embedpostgresql/");
      try {
        return postgres.start(EmbeddedPostgres.cachedRuntimeConfig(cachePath));
      } catch (Exception e) {
        throw new InternalError(e);
      }
    }

    @Override
    public void stop() {
      postgres.stop();
    }
  };

  static PostgreSQLSetup get() {
    PostgreSQLSetup h;
    switch (BuildEnvironment.get()) {
      case AppVeyor:
        h = AppVeyorHandler;
        break;
      case Travis:
        if (System.getenv().getOrDefault("POSTGRESQL_SERVICE", "false").equals("true")) {
          h =  TravisHandler;
          break;
        }
      default:
        h = EmbeddedHandler;
    }
    return h;
  }

}
