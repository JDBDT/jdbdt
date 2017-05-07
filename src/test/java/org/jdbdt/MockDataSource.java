package org.jdbdt;

@SuppressWarnings("javadoc")
class MockDataSource extends DataSource {
  MockDataSource(String... columns) {
    super(columns);
  }
}
