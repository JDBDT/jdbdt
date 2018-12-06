/*
 * The MIT License
 *
 * Copyright (c) 2016-2018 Eduardo R. B. Marques
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
      postgres = new EmbeddedPostgres(Version.V10_6);
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
