package ch.admin.bar.siard2.jdbc;

import java.sql.*;
import java.util.*;
import static org.junit.Assert.*;
import org.junit.*;
import ch.enterag.utils.*;
import ch.enterag.utils.base.*;
import ch.enterag.utils.jdbc.*;
import ch.enterag.sqlparser.identifier.*;
import ch.admin.bar.siard2.jdbcx.*;
import ch.admin.bar.siard2.mysql.*;

import org.junit.Test;

public class MySqlResultSetMetaDataTester extends BaseResultSetMetaDataTester 
{
  private static final ConnectionProperties _cp = new ConnectionProperties();
  private static final String _sDB_URL = MySqlDriver.getUrl(_cp.getHost() + ":" + _cp.getPort()+"/"+_cp.getCatalog(),true);
  private static final String _sDB_USER = _cp.getUser();
  private static final String _sDB_PASSWORD = _cp.getPassword();
  private static final String _sDBA_USER = _cp.getDbaUser();
  private static final String _sDBA_PASSWORD = _cp.getDbaPassword();

  private static String getTableQuery(QualifiedId qiTable, List<TestColumnDefinition> listCd)
  {
    StringBuilder sbSql = new StringBuilder("SELECT\r\n  ");
    for (int iColumn = 0; iColumn < listCd.size(); iColumn++)
    {
      if (iColumn > 0)
        sbSql.append(",\r\n  ");
      TestColumnDefinition tcd = listCd.get(iColumn);
      sbSql.append(tcd.getName());
    }
    sbSql.append("\r\nFROM ");
    sbSql.append(qiTable.format());
    return sbSql.toString();
  } /* getTableQuery */
  
  private static String _sNativeQuerySimple = getTableQuery(TestMySqlDatabase.getQualifiedSimpleTable(),TestMySqlDatabase._listCdSimple);
  private static String _sNativeQueryComplex = getTableQuery(TestMySqlDatabase.getQualifiedComplexTable(),TestMySqlDatabase._listCdComplex);
  private static String _sSqlQuerySimple = getTableQuery(TestSqlDatabase.getQualifiedSimpleTable(),TestSqlDatabase._listCdSimple);
  private static String _sSqlQueryComplex = getTableQuery(TestSqlDatabase.getQualifiedComplexTable(),TestSqlDatabase._listCdComplex);

	@BeforeClass 
	public static void setUpClass()
	{
		try
		{
			MySqlDataSource dsMySql = new MySqlDataSource();
			dsMySql.setUrl(_sDB_URL);
			dsMySql.setUser(_sDBA_USER);
			dsMySql.setPassword(_sDBA_PASSWORD);
			MySqlConnection connMySql = (MySqlConnection) dsMySql.getConnection();
      /* drop and create the test databases */
      new TestMySqlDatabase(connMySql);
      TestMySqlDatabase.grantSchemaUser(connMySql, 
        TestMySqlDatabase._sTEST_SCHEMA, _sDB_USER);
      new TestSqlDatabase(connMySql);
      TestMySqlDatabase.grantSchemaUser(connMySql, 
        TestSqlDatabase._sTEST_SCHEMA, _sDB_USER);
      connMySql.close();
		}
		catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
	} /* setUpClass */

  private Connection closeResultSet()
    throws SQLException
  {
    Connection conn = null;
    ResultSet rs = getResultSet();
    if (rs != null)
    {
      if (!rs.isClosed())
      {
        Statement stmt = rs.getStatement();
        rs.close();
        setResultSetMetaData(null,null);
        if (!stmt.isClosed())
        {
          conn = stmt.getConnection();
          stmt.close();
        }
      }
    }
    return conn;
  } /* closeResultSet */

  private void openResultSet(Connection conn, String sQuery)
    throws SQLException
  {
    closeResultSet();
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(sQuery);
    ResultSetMetaData rsmd = rs.getMetaData();
    setResultSetMetaData(rsmd,rs);
  } /* openResultSet */
  
	@Before
	public void setUp() {
		try 
		{
			MySqlDataSource dsMySql = new MySqlDataSource();
			dsMySql.setUrl(_sDB_URL);
			dsMySql.setUser(_sDB_USER);
			dsMySql.setPassword(_sDB_PASSWORD);
			Connection conn = dsMySql.getConnection();
			conn.setAutoCommit(false);
      openResultSet(conn,_sNativeQuerySimple);
		}
		catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
	}

  @After
  @Override
  public void tearDown()
 {
    try
    {
      Connection conn = closeResultSet();
      if (conn != null)
      {
        if (!conn.isClosed())
        {
          conn.commit();
          conn.close();
        }
      }
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* tearDown */

	@Test
	public void testClass()
	{
		assertEquals("Wrong result set metadata class!", MySqlResultSetMetaData.class, getResultSetMetaData().getClass());
	} /* testClass */

  @Test
  public void testNativeSimple()
  {
    try
    {
      openResultSet(getResultSet().getStatement().getConnection(),_sNativeQuerySimple);
      super.testAll();
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testNativeSimple */

  @Test
  public void testNativeComplex()
  {
    try
    {
      openResultSet(getResultSet().getStatement().getConnection(),_sNativeQueryComplex);
      super.testAll();
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testNativeComplex */

  @Test
  public void testSqlSimple()
  {
    try
    {
      openResultSet(getResultSet().getStatement().getConnection(),_sSqlQuerySimple);
      super.testAll();
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testSqlSimple */

  @Test
  public void testSqlComplex()
  {
    try
    {
      openResultSet(getResultSet().getStatement().getConnection(),_sSqlQueryComplex);
      super.testAll();
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testSqlComplex */
  
}
