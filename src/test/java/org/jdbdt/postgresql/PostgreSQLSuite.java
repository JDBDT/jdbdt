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

package org.jdbdt.postgresql;

import org.jdbdt.DBConfig;
import org.jdbdt.DBEngineTestSuite;
import org.jdbdt.TestCategories;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.ExcludeCategory;
import org.junit.runner.RunWith;

@SuppressWarnings("javadoc")
@RunWith(Categories.class)
@ExcludeCategory({
  TestCategories.CaseSensitive.class,
  //TestCategories.CSVSupport.class
})
public class PostgreSQLSuite extends DBEngineTestSuite {

  @BeforeClass 
  public static void setup() { 
    DBConfig.getConfig()
    .reset()
    .setDriver("org.postgresql.Driver")
    .setURL(PostgreSQLSetup.get().start());
  }

  @AfterClass
  public static void teardown() {
    PostgreSQLSetup.get().stop();
  }
}
