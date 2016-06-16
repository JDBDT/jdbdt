package org.jdbdt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("javadoc")
class UserDAO {
  static final String TABLE_NAME = "Users";

  static final String[] COLUMNS = { 
    "login", "name", "password", "created" 
  };

  private final Connection connection;

  public UserDAO(Connection c) throws SQLException {
    connection = c;
    try {
      stmt(Op.DROP).execute();
    } 
    catch(SQLException e) { }
    stmt(Op.CREATE).execute();
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
