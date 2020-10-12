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

package org.jdbdt.hsqlsb;

import org.jdbdt.DBConfig;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({ FileDBTest.class, MemDBTest.class })
public class HSQLDBSuite {
  public static final String DRIVER = "org.hsqldb.jdbcDriver";
  public static final String MEM_DB_URL = "jdbc:hsqldb:mem:jdbdt-hsqldb-test;shutdown=true";
  public static final String FILE_DB_URL = "jdbc:hsqldb:file:jdbdt-hsqldb-test;shutdown=true";
  
  @BeforeClass 
  public static void setup() throws ClassNotFoundException { 
    DBConfig.getConfig().reset().setDriver(DRIVER);
  }
}
