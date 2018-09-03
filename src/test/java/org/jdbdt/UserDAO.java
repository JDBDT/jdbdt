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

package org.jdbdt;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("javadoc")
class UserDAO {
  static final String TABLE_NAME = "Users";

  static final String[] COLUMNS = { 
      "login", "name", "password", "created" 
  };

  public static final String PRIMARY_KEY = "LOGIN";

  private final Connection connection;

  public UserDAO(Connection c) throws SQLException {
    connection = c;
    try {
      dropTable();
    } 
    catch(SQLException e) { 

    }
    createTable();

  }

  private PreparedStatement stmt(Op op) throws SQLException {
    return connection.prepareStatement(op.getSQL());
  }

  @SafeVarargs
  public final void doInsert(User... users) throws SQLException {
    try(PreparedStatement s = stmt(Op.INSERT)) {
      for (User u : users) {
        s.setString(1, u.getLogin());
        s.setString(2, u.getName());
        s.setString(3, u.getPassword());
        s.setDate(4, u.getCreated());
        s.execute();
      }
    }
  }
  public void createTable() throws SQLException {
    stmt(Op.CREATE).execute();
  }

  public void dropTable() throws SQLException {
    stmt(Op.DROP).execute();
  }

  public boolean tableExists() throws SQLException {
    DatabaseMetaData dbmd = connection.getMetaData();
    try(ResultSet rs = dbmd.getTables(null, null, TABLE_NAME, new String[] {"TABLE"})) {
      return rs.next();
    }
  }
  
  public int doDeleteAll() throws SQLException {
    try (PreparedStatement stmt = stmt(Op.DELETE_ALL)) {
      return stmt.executeUpdate();
    }   
  }

  @SafeVarargs
  public final int doDelete(String... ids) throws SQLException {
    int n = 0;
    try(PreparedStatement s = stmt(Op.DELETE)) {
      for (String id : ids) {
        s.setString(1, id);
        n += s.executeUpdate();
      }
      return n;
    }
  }

  @SafeVarargs
  public final int doUpdate(User... users) throws SQLException {
    int n = 0;
    try (PreparedStatement s = stmt(Op.UPDATE)) {
      for (User u : users) {
        s.setString(1, u.getName());
        s.setString(2, u.getPassword());
        s.setDate(3, u.getCreated());
        s.setString(4, u.getLogin());
        n += s.executeUpdate();
      }
    }
    return n;
  }

  public User query(String id) throws SQLException {
    try(PreparedStatement s = stmt(Op.SELECT)) {
      s.setString(1, id);
      try (ResultSet rs = s.executeQuery()) {
        return rs.next() ? 
            new User(id, 
                rs.getString(1), 
                rs.getString(2),
                rs.getDate(3)) 
            : null;
      } 
    }
  }

  public int count() throws SQLException {
    try(ResultSet rs = stmt(Op.COUNT).executeQuery()) {
      rs.next();
      return rs.getInt(1);
    } 
  }


  private enum Op { 
    DROP("DROP TABLE %s"),
    CREATE("CREATE TABLE %s ("
        + "LOGIN VARCHAR(10) PRIMARY KEY NOT NULL,"
        + "NAME VARCHAR(40) NOT NULL, " + "PASSWORD VARCHAR(32) NOT NULL,"
        + "CREATED DATE)"),
    DELETE_ALL("DELETE FROM %s"),
    DELETE("DELETE FROM %s WHERE login = ?"),
    INSERT("INSERT INTO %s(login, name, password, created) VALUES (?,?,?,?)"),
    SELECT("SELECT name, password, created FROM %s WHERE LOGIN = ? "),
    UPDATE("UPDATE %s set name=?,password=?,created=? WHERE login=?"),
    COUNT("SELECT COUNT(*) FROM %s");

    private String sql; 

    Op(String sqlFmt) {
      this.sql = String.format(sqlFmt, TABLE_NAME);
    }

    String getSQL() {
      return sql;
    }
  }
}
