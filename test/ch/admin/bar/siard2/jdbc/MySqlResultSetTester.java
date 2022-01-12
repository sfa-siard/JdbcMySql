package ch.admin.bar.siard2.jdbc;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import javax.xml.datatype.*;
import static org.junit.Assert.*;
import org.junit.*;

import ch.enterag.utils.*;
import ch.enterag.utils.base.*;
import ch.enterag.utils.jdbc.*;
import ch.enterag.sqlparser.*;
import ch.enterag.sqlparser.identifier.*;
import ch.admin.bar.siard2.jdbcx.*;
import ch.admin.bar.siard2.mysql.*;

public class MySqlResultSetTester extends BaseResultSetTester 
{
  private static final ConnectionProperties _cp = new ConnectionProperties();
  private static final String _sDB_URL = MySqlDriver.getUrl(_cp.getHost() + ":" + _cp.getPort()+"/"+_cp.getCatalog(),true);
  private static final String _sDB_USER = _cp.getUser();
  private static final String _sDB_PASSWORD = _cp.getPassword();
  private static final String _sDBA_USER = _cp.getDbaUser();
  private static final String _sDBA_PASSWORD = _cp.getDbaPassword();
  private static long _lMsTotalStart = 0;
  private static long _lMsTotal = 0;
  private static long _lMsConnect = 0;
  private static long _lMsExecute = 0;
  private static long _lMsTestDatabase = 0;

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
 	
  @SuppressWarnings("deprecation")
  private static List<TestColumnDefinition> getListCdSimple()
  {
    List<TestColumnDefinition> listCdSimple = new ArrayList<TestColumnDefinition>();
    listCdSimple.add(new TestColumnDefinition("CCHAR_5","CHAR(5)","wxyZ"));
    listCdSimple.add(new TestColumnDefinition("CVARCHAR_255","VARCHAR(255)",TestUtils.getString(92)));
    listCdSimple.add(new TestColumnDefinition("CCLOB_2M","CLOB(2M)",TestUtils.getString(1000000)));
    listCdSimple.add(new TestColumnDefinition("CNCHAR_5","NCHAR(5)","Auää"));
    listCdSimple.add(new TestColumnDefinition("CNVARCHAR_127","NCHAR VARYING(127)",TestUtils.getNString(53)));
    listCdSimple.add(new TestColumnDefinition("CNCLOB_1M","NCLOB(1M)",TestUtils.getNString(500000)));
    listCdSimple.add(new TestColumnDefinition("CXML","XML","<a>foöäpwkfèégopàèwerkgv fviodsjv jdsjd idsjidsjsiudojiou operkv &lt; and &amp; ifjeifj</a>"));
    listCdSimple.add(new TestColumnDefinition("CBINARY_5","BINARY(5)",new byte[] {5,-4,3,-2} ));
    listCdSimple.add(new TestColumnDefinition("CVARBINARY_255","VARBINARY(255)",TestUtils.getBytes(76) ));
    listCdSimple.add(new TestColumnDefinition("CBLOB","BLOB",TestUtils.getBytes(500000)));
    listCdSimple.add(new TestColumnDefinition("CNUMERIC_31","NUMERIC(31)",BigInteger.valueOf(987654321098765432l)));
    listCdSimple.add(new TestColumnDefinition("CDECIMAL_15_5","DECIMAL(15,5)",new BigDecimal(BigInteger.valueOf(9876543210987l),5)));
    listCdSimple.add(new TestColumnDefinition("CSMALLINT","SMALLINT",Short.valueOf((short)23000)));
    listCdSimple.add(new TestColumnDefinition("CINTEGER","INTEGER",Integer.valueOf(987654321)));
    listCdSimple.add(new TestColumnDefinition("CBIGINT","BIGINT",Long.valueOf(-987654321098765432l)));
    listCdSimple.add(new TestColumnDefinition("CFLOAT_10","FLOAT(10)",Float.valueOf((float)Math.PI)));
    listCdSimple.add(new TestColumnDefinition("CREAL","REAL",Float.valueOf((float)Math.E)));
    listCdSimple.add(new TestColumnDefinition("CDOUBLE","DOUBLE PRECISION",Double.valueOf(Math.PI)));
    listCdSimple.add(new TestColumnDefinition("CBOOLEAN","BOOLEAN",Boolean.valueOf(false)));
    listCdSimple.add(new TestColumnDefinition("CDATE","DATE",new Date(2016-1900,12,2)));
    listCdSimple.add(new TestColumnDefinition("CTIME","TIME",new Time(14,24,12)));
    listCdSimple.add(new TestColumnDefinition("CTIMESTAMP","TIMESTAMP(9)",new Timestamp(2016-1900,12,2,14,24,12,987654321)));
    listCdSimple.add(new TestColumnDefinition("CINTERVAL_YEAR_3_MONTH","INTERVAL YEAR(2) TO MONTH",new Interval(1,3,6)));
    listCdSimple.add(new TestColumnDefinition("CINTERVAL_DAY_2_SECONDS_6","INTERVAL DAY(2) TO SECOND(6)",new Interval(1,0,17,54,23,123456000l)));
    return listCdSimple;
  }
  public static List<TestColumnDefinition> _listCdSimple = getListCdSimple();

  private static List<TestColumnDefinition> getListCdComplex()
  {
    List<TestColumnDefinition> listCdComplex = new ArrayList<TestColumnDefinition>();
    listCdComplex.add(new TestColumnDefinition("CID","INTEGER",Integer.valueOf(987654321)));
    return listCdComplex;
  }
  public static List<TestColumnDefinition> _listCdComplex = getListCdComplex();
  
  
	@BeforeClass 
	public static void setUpClass()
	{
	  System.out.println("Creating test databases");
    if (_lMsTotalStart == 0)
      _lMsTotalStart = System.currentTimeMillis(); 
    try
    {
      MySqlDataSource dsMySql = new MySqlDataSource();
      dsMySql.setUrl(_sDB_URL);
      dsMySql.setUser(_sDBA_USER);
      dsMySql.setPassword(_sDBA_PASSWORD);
      MySqlConnection connMySql = (MySqlConnection) dsMySql.getConnection();
      /* drop and create the test databases */
      long lMsTestDatabaseStart = System.currentTimeMillis();
      new TestMySqlDatabase(connMySql);
      TestMySqlDatabase.grantSchemaUser(connMySql, 
        TestMySqlDatabase._sTEST_SCHEMA, _sDB_USER);
      new TestSqlDatabase(connMySql);
      TestMySqlDatabase.grantSchemaUser(connMySql, 
        TestSqlDatabase._sTEST_SCHEMA, _sDB_USER);
      _lMsTestDatabase = _lMsTestDatabase + System.currentTimeMillis() - lMsTestDatabaseStart;
      connMySql.close();
      System.out.println("Test databases created");
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
	} /* setUpClass */
	
  @AfterClass
  public static void tearDownClass()
  {
    _lMsTotal = System.currentTimeMillis() - _lMsTotalStart;
    System.out.println("Total: "+String.valueOf(_lMsTotal)+", Connect: "+String.valueOf(_lMsConnect)+", Execute: "+String.valueOf(_lMsExecute)+", Test Database: "+String.valueOf(_lMsTestDatabase));
  }
  
  private Connection _conn = null;
  
  private Connection closeResultSet()
    throws SQLException
  {
    ResultSet rs = getResultSet();
    if (rs != null)
    {
      if (!rs.isClosed())
      {
        Statement stmt = rs.getStatement();
        rs.close();
        setResultSet(null);
        if (!stmt.isClosed())
          stmt.close();
        _conn.commit();
      }
    }
    return _conn;
  } /* closeResultSet */
  
  private void openResultSet(String sQuery, int iType, int iConcurrency)
    throws SQLException
  {
    closeResultSet();
    Statement stmt = _conn.createStatement(iType,iConcurrency);
    long lMsExecuteStart = System.currentTimeMillis();
    ResultSet rs = stmt.executeQuery(sQuery);
    // it is always TYPE_FORWARD_ONLY
    // assertEquals("Invalid result set type!",iType,rs.getType());
    assertEquals("Invalid result set concurrency!",iConcurrency,rs.getConcurrency());
    // assertEquals("Invalid result set holdability",ResultSet.CLOSE_CURSORS_AT_COMMIT,rs.getHoldability());
    _lMsExecute = _lMsExecute + System.currentTimeMillis() - lMsExecuteStart; 
    setResultSet(rs);
    rs.next();
  } /* openResultSet */
  
	@Before
	public void setUp() {
    try 
    {
      MySqlDataSource dsMySql = new MySqlDataSource();
      dsMySql.setUrl(_sDB_URL);
      dsMySql.setUser(_sDB_USER);
      dsMySql.setPassword(_sDB_PASSWORD);
      long lMsConnectStart = System.currentTimeMillis();
      _conn = dsMySql.getConnection();
      _lMsConnect = _lMsConnect + System.currentTimeMillis() - lMsConnectStart;
      _conn.setAutoCommit(false);
      //openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
	}
	
  @After
  public void tearDown()
  {
    try
    {
      closeResultSet();
      if (!_conn.isClosed())
      {
        _conn.commit();
        _conn.close();
      }
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* tearDown */
  
  private TestColumnDefinition findColumnDefinition(List<TestColumnDefinition> listCd, String sName)
  {
    TestColumnDefinition tcd = null;
    for (Iterator<TestColumnDefinition> iterCd = listCd.iterator(); iterCd.hasNext(); )
    {
      TestColumnDefinition tcdTry = iterCd.next();
      if (sName.equals(tcdTry.getName()))
        tcd = tcdTry;
    }
    return tcd;
  } /* findColumnDefinition */
  
	@Test
	public void testClass() 
	{
	  try
	  {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
  	    assertEquals("Wrong result set class!", MySqlResultSet.class, getResultSet().getClass());
	  }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
	}
	
  @Override
  @Test
  public void testFindColumn()
  {
    enter();
    try 
    { 
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().findColumn("CCHAR_5");
    }
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testFindColumn */
  
  @Override
  @Test
  public void testWasNull()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().getObject(1);
      getResultSet().wasNull();
    }
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testWasNull */

  @Override
  @Test
  public void testGetString()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CVARCHAR_255");
      String s = getResultSet().getString(tcd.getName());
      assertEquals("Invalid String!",(String)tcd.getValue(),s);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetString */
  
  @Override
  @Test
  public void testGetNString()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CNVARCHAR_127");
      String s = getResultSet().getNString(tcd.getName());
      assertEquals("Invalid String!",(String)tcd.getValue(),s);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetNString */
  
  @Override
  @Test
  public void testGetClob()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CCLOB_2M");
      Clob clob = getResultSet().getClob(tcd.getName());
      String s = clob.getSubString(1l,(int)clob.length());
      assertEquals("Invalid Clob!",(String)tcd.getValue(),s);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetClob */
  
  @Override
  @Test
  public void testGetNClob()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CNCLOB_1M");
      NClob nclob = getResultSet().getNClob(tcd.getName());
      String s = nclob.getSubString(1l,(int)nclob.length());
      assertEquals("Invalid NClob!",(String)tcd.getValue(),s);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetNClob */
  
  @Override
  @Test
  public void testGetSqlXml()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CXML");
      SQLXML sqlxml = getResultSet().getSQLXML(tcd.getName());
      String s = sqlxml.getString();
      assertEquals("Invalid SQLXML!",(String)tcd.getValue(),s);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetSqlXml */
  
  @Override
  @Test
  public void testGetBytes()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CVARBINARY_255");
      byte[] buf = getResultSet().getBytes(tcd.getName());
      assertTrue("Invalid byte array!",Arrays.equals((byte[])tcd.getValue(),buf));
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetBytes */
  
  @Override
  @Test
  public void testGetBlob()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CBLOB");
      Blob blob = getResultSet().getBlob(tcd.getName());
      byte[] buf = blob.getBytes(1l,(int)blob.length());
      assertTrue("Invalid Blob!",Arrays.equals((byte[])tcd.getValue(),buf));
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetBlob */
  
  @Override
  @Test
  public void testGetBigDecimal()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CDECIMAL_15_5");
      BigDecimal bd = getResultSet().getBigDecimal(tcd.getName());
      assertEquals("Invalid BigDecimal!",(BigDecimal)tcd.getValue(),bd);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetBigDecimal */
  
  @Override
  @Test
  @SuppressWarnings("deprecation")
  public void testGetBigDecimal_Int()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CDECIMAL_15_5");
      BigDecimal bd = getResultSet().getBigDecimal(tcd.getName(),3);
      assertEquals("Invalid BigDecimal!",((BigDecimal)tcd.getValue()).setScale(3,RoundingMode.DOWN),bd);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetBigDecimal_String_Int */
  
  @Override
  @Test
  public void testGetByte()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CBOOLEAN");
      byte by = getResultSet().getByte(tcd.getName());
      assertEquals("Invalid byte!",((Boolean)tcd.getValue()).booleanValue()?(byte)1:(byte)0,by);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetByte */

  @Override
  @Test
  public void testGetShort()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CSMALLINT");
      short sh = getResultSet().getShort(tcd.getName());
      assertEquals("Invalid short!",((Short)tcd.getValue()).shortValue(),sh);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetShort */
  
  @Override
  @Test
  public void testGetInt()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CINTEGER");
      int i = getResultSet().getInt(tcd.getName());
      assertEquals("Invalid int!",((Integer)tcd.getValue()).intValue(),i);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetInt */
  
  @Override
  @Test
  public void testGetLong()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CBIGINT");
      long l = getResultSet().getLong(tcd.getName());
      assertEquals("Invalid long!",((Long)tcd.getValue()).longValue(),l);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetLong */
  
  @Override
  @Test
  public void testGetFloat()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CREAL");
      float f = getResultSet().getFloat(tcd.getName());
      assertEquals("Invalid float!",(Float)tcd.getValue(),Float.valueOf(f),0.00001);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetFloat */
  
  @Override
  @Test
  public void testGetDouble()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CDOUBLE");
      double d = getResultSet().getDouble(tcd.getName());
      assertEquals("Invalid double!",(Double)tcd.getValue(),Double.valueOf(d));
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetDouble */
  
  @Override
  @Test
  public void testGetBoolean()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CBOOLEAN");
      boolean b = getResultSet().getBoolean(tcd.getName());
      assertEquals("Invalid boolean!",((Boolean)tcd.getValue()).booleanValue(),b);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetBoolean */
  
  @Override
  @Test
  public void testGetDate()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CDATE");
      Date date = getResultSet().getDate(tcd.getName());
      assertEquals("Invalid Date!",(Date)tcd.getValue(),date);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetDate */
  
  @Override
  @Test
  public void testGetDate_Calendar()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CDATE");
      Calendar cal = new GregorianCalendar();
      Date date = getResultSet().getDate(tcd.getName(),cal);
      assertEquals("Invalid Date!",(Date)tcd.getValue(),date);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetDate_Calendar */
  
  @Override
  @Test
  public void testGetTime()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CTIME");
      Time time = getResultSet().getTime(tcd.getName());
      assertEquals("Invalid Time!",(Time)tcd.getValue(),time);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetTime */
  
  @Override
  @Test
  public void testGetTime_Calendar()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CTIME");
      Calendar cal = new GregorianCalendar();
      Time time = getResultSet().getTime(tcd.getName(),cal);
      assertEquals("Invalid Time!",(Time)tcd.getValue(),time);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetTime_Calendar */
  
  @Override
  @Test
  public void testGetTimestamp()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CTIMESTAMP");
      Object o = getResultSet().getObject(tcd.getName());
      System.out.println(o);
      Timestamp ts = getResultSet().getTimestamp(tcd.getName());
      Timestamp tsExpected = (Timestamp)tcd.getValue();
      /* only 6 fractional seconds digits in MySQL */
      tsExpected.setNanos(1000*((tsExpected.getNanos()+499)/1000));
      assertEquals("Invalid Timestamp!",tsExpected,ts);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetTimestamp */

  @Override
  @Test
  public void testGetTimestamp_Calendar()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CTIMESTAMP");
      Calendar cal = new GregorianCalendar();
      Timestamp ts = getResultSet().getTimestamp(tcd.getName(),cal);
      Timestamp tsExpected = (Timestamp)tcd.getValue();
      /* only 6 fractional seconds digits in MySQL */
      tsExpected.setNanos(1000*((tsExpected.getNanos()+499)/1000));
      assertEquals("Invalid Timestamp!",tsExpected,ts);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetTimestamp_Calendar */
  
  @Test
  public void testGetDuration()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CINTERVAL_YEAR_3_MONTH");
      Duration duration = getBaseResultSet().getDuration(tcd.getName());
      assertEquals("Invalid Duration!",(Interval)tcd.getValue(),Interval.fromDuration(duration));
    }
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  }
  
  @Override
  @Test
  public void testGetAsciiStream()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CVARCHAR_255");
      InputStream is = getResultSet().getAsciiStream(tcd.getName());
      byte[] buf = new byte[((String)tcd.getValue()).length()];
      is.read(buf);
      if (is.read() != -1)
        fail("Invalid length of ASCII stream!");
      is.close();
      String s = new String(buf);
      assertEquals("Invalid String!",(String)tcd.getValue(),s);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
    catch(IOException ie) { fail(EU.getExceptionMessage(ie)); }
  } /* testGetAsciiStream */
  
  @Override
  @Test
  @SuppressWarnings("deprecation")
  public void testGetUnicodeStream()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CNVARCHAR_127");
      Reader rdr = new InputStreamReader(getResultSet().getUnicodeStream(tcd.getName()),SU.sUTF8_CHARSET_NAME);
      char[] cbuf = new char[((String)tcd.getValue()).length()];
      rdr.read(cbuf);
      if (rdr.read() != -1)
        fail("Invalid length of character stream!");
      rdr.close();
      String s = new String(cbuf);
      assertEquals("Invalid String!",(String)tcd.getValue(),s);
    }
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
    catch(IOException ie) { fail(EU.getExceptionMessage(ie)); }
  } /* testGetUnicodeStream */
  
  @Override
  @Test
  public void testGetCharacterStream()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CVARCHAR_255");
      Reader rdr = getResultSet().getCharacterStream(tcd.getName());
      char[] cbuf = new char[((String)tcd.getValue()).length()];
      rdr.read(cbuf);
      if (rdr.read() != -1)
        fail("Invalid length of character stream!");
      rdr.close();
      String s = new String(cbuf);
      assertEquals("Invalid String!",(String)tcd.getValue(),s);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
    catch(IOException ie) { fail(EU.getExceptionMessage(ie)); }
  } /* testGetCharacterStream */
  
  @Override
  @Test
  public void testGetNCharacterStream()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CNVARCHAR_127");
      Reader rdr = getResultSet().getNCharacterStream(tcd.getName());
      char[] cbuf = new char[((String)tcd.getValue()).length()];
      rdr.read(cbuf);
      if (rdr.read() != -1)
        fail("Invalid length of character stream!");
      rdr.close();
      String s = new String(cbuf);
      assertEquals("Invalid String!",(String)tcd.getValue(),s);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
    catch(IOException ie) { fail(EU.getExceptionMessage(ie)); }
  } /* testGetNCharacterStream */
  
  @Override
  @Test
  public void testGetBinaryStream()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CVARBINARY_255");
      InputStream is = getResultSet().getBinaryStream(tcd.getName());
      byte[] buf = new byte[((byte[])tcd.getValue()).length];
      is.read(buf);
      if (is.read() != -1)
        fail("Invalid length of binary stream!");
      is.close();
      assertTrue("Invalid byte array!",Arrays.equals((byte[])tcd.getValue(),buf));
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
    catch(IOException ie) { fail(EU.getExceptionMessage(ie)); }
  } /* testGetBinaryStream */
  
  @Override
  @Test
  public void testGetArray()
  {
    // no arrays in DB/2 tables
  } /* testGetArray */
  
  @Override
  @Test
  public void testGetRef()
  {
  } /* testGetRef */
  
  @Override
  @Test
  public void testGetRowId()
  {
  } /* testGetRowId */
  
  @Override
  @Test
  public void testGetUrl()
  {
  } /* testGetUrl */
  
  @Override
  @Test
  public void testGetObject()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CINTERVAL_YEAR_3_MONTH");
      Object o = getResultSet().getObject(tcd.getName());
      Interval iv = SqlLiterals.deserialize((byte[])o, Interval.class);
      assertEquals("Invalid Interval!",(Interval)tcd.getValue(),iv);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetObject */

  @Override
  @Test
  public void testGetObject_Class()
  {
    enter();
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdSimple,"CDATE");
      Date date = getResultSet().getObject(tcd.getName(),Date.class);
      assertEquals("Invalid Date!",(Date)tcd.getValue(),date);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetObject_Class */
  
  @Override
  @Test
  public void testGetObject_Map()
  {
    try
    {
      openResultSet(_sSqlQueryComplex,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      TestColumnDefinition tcd = findColumnDefinition(
        TestSqlDatabase._listCdComplex,"CID");
      Map<String,Class<?>> map = new HashMap<String,Class<?>>();
      map.put(tcd.getType(), tcd.getValue().getClass());
      Object o = getResultSet().getObject(tcd.getName(),map);
      assertEquals("Invalid CID!",tcd.getValue(),o);
    }
    catch(SQLFeatureNotSupportedException snse) { System.out.println(EU.getExceptionMessage(snse)); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetObject_Map */
  
  @Test
  public void testGetObjectSqlSimple()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      for (int iColumn = 0; iColumn < TestSqlDatabase._listCdSimple.size(); iColumn++)
      {
        TestColumnDefinition tcd = TestSqlDatabase._listCdSimple.get(iColumn);
        Object o = getResultSet().getObject(tcd.getName());
        if (tcd.getName().equals("CCHAR_5") ||
            tcd.getName().equals("CNCHAR_5"))
        {
          if (o instanceof String)
          {
            String s = (String)o;
            s = s.substring(0,((String)tcd.getValue()).length());
            assertEquals("Invalid value for "+tcd.getType()+"!",tcd.getValue(),s);
          }
          else
            fail("Type String expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CVARCHAR_255") ||
          tcd.getName().equals("CNVARCHAR_127"))
        {
          if (o instanceof String)
          {
            String s = (String)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",tcd.getValue(),s);
          }
          else
            fail("Type String expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CCLOB_2M") ||
          tcd.getName().equals("CNCLOB_1M") ||
          tcd.getName().equals("CXML"))
        {
          if (o instanceof Clob)
          {
            Clob clob = (Clob)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",tcd.getValue(),clob.getSubString(1l,(int)clob.length()));
          }
          else
            fail("Type Clob expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CBINARY_5"))
        {
          if (o instanceof byte[])
          {
            byte[] buf = (byte[])o;
            buf = Arrays.copyOf(buf, ((byte[])tcd.getValue()).length);
            assertTrue("Invalid value for "+tcd.getType()+"!",Arrays.equals((byte[])tcd.getValue(), buf));
          }
          else
            fail("Type byte[] expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CVARBINARY_255"))
        {
          if (o instanceof byte[])
          {
            byte[] buf = (byte[])o;
            assertTrue("Invalid value for "+tcd.getType()+"!",Arrays.equals((byte[])tcd.getValue(), buf));
          }
          else
            fail("Type byte[] expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CBLOB"))
        {
          if ( o instanceof Blob)
          {
            Blob blob = (Blob)o;
            assertTrue("Invalid value for "+tcd.getType()+"!",Arrays.equals((byte[])tcd.getValue(),blob.getBytes(1l,(int)blob.length())));
          }
          else
            fail("Type Blob expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CDECIMAL_15_5") ||
          tcd.getName().equals("CNUMERIC_31"))
        {
          if (o instanceof BigDecimal)
          {
            BigDecimal bd = (BigDecimal)o;
            Object oExpected = tcd.getValue();
            if (oExpected instanceof BigDecimal)
            {
              BigDecimal bdExpected = (BigDecimal)o;
              assertEquals("Invalid value for "+tcd.getType()+"!",bdExpected,bd);
            }
            else if (oExpected instanceof BigInteger)
            {
              BigInteger biExpected = (BigInteger)oExpected;
              BigInteger bi = bd.toBigInteger();
              assertEquals("Invalid value for "+tcd.getType()+"!",biExpected,bi);
            }
          }
          else
            fail("Type BigDecimal expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CSMALLINT"))
        {
          if (o instanceof Short)
          {
            Short sh = (Short)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Short)tcd.getValue(),sh);
          }
          else
            fail("Type Short expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CINTEGER"))
        {
          if (o instanceof Integer)
          {
            Integer i = (Integer)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Integer)tcd.getValue(),i);
          }
          else
            fail("Type Integer expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CBIGINT"))
        {
          if (o instanceof Long)
          {
            Long l = (Long)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Long)tcd.getValue(),l);
          }
          else
            fail("Type Long expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CREAL") ||
                 tcd.getName().equals("CFLOAT_10"))
        {
          if (o instanceof Float)
          {
            Float f = (Float)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Float)tcd.getValue(),f,0.00001f);
          }
          else
            fail("Type Float expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CDOUBLE"))
        {
          if (o instanceof Double)
          {
            Double d = (Double)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Double)tcd.getValue(),d);
          }
          else
            fail("Type Double expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CBOOLEAN"))
        {
          if (o instanceof Short)
          {
            Short sh = (Short)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Boolean)tcd.getValue(),Boolean.valueOf(sh != 0));
          }
        }
        else if (tcd.getName().equals("CDATE"))
        {
          if (o instanceof Date)
          {
            Date date = (Date)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Date)tcd.getValue(),date);
          }
          else
            fail("Type Date expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CTIME"))
        {
          if (o instanceof Time)
          {
            Time time = (Time)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Time)tcd.getValue(),time);
          }
          else
            fail("Type Time expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CTIMESTAMP"))
        {
          if (o instanceof Timestamp)
          {
            Timestamp ts = (Timestamp)o;
            Timestamp tsExpected = (Timestamp)tcd.getValue();
            /* only 6 significant decimals */
            tsExpected.setNanos(1000*((tsExpected.getNanos()+499)/1000));
            assertEquals("Invalid value for "+tcd.getType()+"!",(Timestamp)tcd.getValue(),ts);
          }
          else
            fail("Type Timestamp expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CINTERVAL_YEAR_3_MONTH") ||
                 tcd.getName().equals("CINTERVAL_DAY_2_SECONDS_6"))
        {
          if (o instanceof byte[])
          {
            byte[] buf= (byte[])o;
            Interval iv = SqlLiterals.deserialize(buf, Interval.class);
            assertEquals("Invalid value for "+tcd.getType()+"!",(Interval)tcd.getValue(),iv);
          }
          else
            fail("Type byte[] expected for "+tcd.getType()+"!");
        }
        else
          fail("Unexpected column: "+tcd.getName()+"!");
      }
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetObjectSqlSimple */
  
  @Test
  public void testGetObjectNativeSimple()
  {
    try
    {
      openResultSet(_sNativeQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      for (int iColumn = 0; iColumn < TestMySqlDatabase._listCdSimple.size(); iColumn++)
      {
        TestColumnDefinition tcd = TestMySqlDatabase._listCdSimple.get(iColumn);
        Object o = getResultSet().getObject(tcd.getName());
        if (tcd.getName().equals("CINT") ||
          tcd.getName().equals("CINTEGER") ||
          tcd.getName().equals("CMEDIUMINT") ||
          tcd.getName().equals("CMEDIUMINT_U") ||
          tcd.getName().equals("CSMALLINT_U"))
        {
          if (o instanceof Integer)
          {
            Integer i = (Integer)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Integer)tcd.getValue(),i);
          }
          else
            fail("Type Integer expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CBIGINT") ||
          tcd.getName().equals("CINT_U") ||
          tcd.getName().equals("CINTEGER_U"))
        {
          if (o instanceof Long)
          {
            Long l = (Long)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Long)tcd.getValue(),l);
          }
          else
            fail("Type Long expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CBIGINT_U"))
        {
          if (o instanceof BigInteger)
          {
            BigInteger bi = (BigInteger)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(BigInteger)tcd.getValue(),bi);
          }
          else
            fail("Type BigInteger expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CSMALLINT") ||
          tcd.getName().equals("CTINYINT_U") ||
          tcd.getName().equals("CTINYINT"))
        {
          if (o instanceof Short)
          {
            Short sh = (Short)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Short)tcd.getValue(),sh);
          }
          else
            fail("Type Short expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CNUMERIC_5_2") ||
          tcd.getName().equals("CDECIMAL_15_5"))
        {
          if (o instanceof BigDecimal)
          {
            BigDecimal bd = (BigDecimal)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(BigDecimal)tcd.getValue(),bd);
          }
          else
            fail("Type BigDecimal expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CFLOAT_9_7") ||
          tcd.getName().equals("CREAL_9_7"))
        {
          if (o instanceof Float)
          {
            Float f = (Float)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Float)tcd.getValue(),f,0.000001f);
          }
          else
            fail("Type Float expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CDOUBLE_16_14"))
        {
          if (o instanceof Double)
          {
            Double d = (Double)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Double)tcd.getValue(),d,0.0000000001);
          }
          else
            fail("Type Double expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CBIT") ||
          tcd.getName().equals("CBOOL"))
        {
          if (o instanceof Boolean)
          {
            Boolean b = (Boolean)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Boolean)tcd.getValue(),b);
          }
          else
            fail("Type Boolean expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CDATE") ||
          tcd.getName().equals("CYEAR"))
        {
          if (o instanceof Date)
          {
            Date date = (Date)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Date)tcd.getValue(),date);
          }
          else
            fail("Type Date expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CTIME"))
        {
          if (o instanceof Time)
          {
            Time time = (Time)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Time)tcd.getValue(),time);
          }
          else
            fail("Type Time expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CTIMESTAMP") ||
          tcd.getName().equals("CDATETIME"))
        {
          if (o instanceof Timestamp)
          {
            Timestamp ts = (Timestamp)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Timestamp)tcd.getValue(),ts);
          }
          else
            fail("Type Timestamp expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CTINYTEXT") ||
          tcd.getName().equals("CVARCHAR_500") ||
          tcd.getName().equals("CNVARCHAR_20000") ||
          tcd.getName().equals("CCHAR_4") ||
          tcd.getName().equals("CNCHAR_50"))
        {
          if (o instanceof String)
          {
            String s = (String)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(String)tcd.getValue(),s);
          }
          else
            fail("Type String expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CTEXT") ||
          tcd.getName().equals("CMEDIUMTEXT") ||
          tcd.getName().equals("CLONGTEXT") ||
          tcd.getName().equals("CLONG_VARCHAR"))
        {
          if (o instanceof Clob)
          {
            Clob clob = (Clob)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(String)tcd.getValue(),clob.getSubString(1l, (int)clob.length()));
          }
          else
            fail("Type Clob expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CTINYBLOB") ||
          tcd.getName().equals("CVARBINARY_255") ||
          tcd.getName().equals("CBINARY_255"))
        {
          if (o instanceof byte[])
          {
            byte[] buf = (byte[])o;
            byte[] bufExpected = (byte[])tcd.getValue();
            buf = Arrays.copyOf(buf, bufExpected.length);
            assertTrue("Invalid value for "+tcd.getType()+"!",
              Arrays.equals(bufExpected,buf));
          }
          else
            fail("Type byte[] expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CBLOB") ||
          tcd.getName().equals("CMEDIUMBLOB") ||
          tcd.getName().equals("CLONGBLOB") ||
          tcd.getName().equals("CLONG_VARBINARY"))
        {
          if (o instanceof Blob)
          {
            Blob blob = (Blob)o;
            assertTrue("Invalid value for "+tcd.getType()+"!",
              Arrays.equals((byte[])tcd.getValue(),blob.getBytes(1l, (int)blob.length())));
          }
          else
            fail("Type Blob expected for "+tcd.getType()+"!");
        }
        else
          fail("Unexpected column: "+tcd.getName()+"!");
      }
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetObjectNativeSimple */

  @Test
  public void testGetObjectSqlComplex()
  {
    try
    {
      openResultSet(_sSqlQueryComplex,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      for (int iColumn = 0; iColumn < TestSqlDatabase._listCdComplex.size(); iColumn++)
      {
        TestColumnDefinition tcd = TestSqlDatabase._listCdComplex.get(iColumn);
        Object o = getResultSet().getObject(tcd.getName());
        if (tcd.getName().equals("CID"))
        {
          if (o instanceof Integer)
          {
            Integer i = (Integer)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Integer)tcd.getValue(),i);
          }
          else
            fail("Type Integer expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CPNG"))
        {
          if ( o instanceof Blob)
          {
            Blob blob = (Blob)o;
            assertTrue("Invalid value for "+tcd.getType()+"!",Arrays.equals((byte[])tcd.getValue(),blob.getBytes(1l,(int)blob.length())));
          }
          else
            fail("Type Blob expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CFLAC"))
        {
          if ( o instanceof Blob)
          {
            Blob blob = (Blob)o;
            assertTrue("Invalid value for "+tcd.getType()+"!",Arrays.equals((byte[])tcd.getValue(),blob.getBytes(1l,(int)blob.length())));
          }
          else
            fail("Type Blob expected for "+tcd.getType()+"!");
        }
        else
          fail("Unexpected column: "+tcd.getName()+"!");
      }
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetObjectSqlComplex */
  
  @Test
  public void testGetObjectNativeComplex()
  {
    try
    {
      openResultSet(_sNativeQueryComplex,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      for (int iColumn = 0; iColumn < TestMySqlDatabase._listCdComplex.size(); iColumn++)
      {
        TestColumnDefinition tcd = TestMySqlDatabase._listCdComplex.get(iColumn);
        Object o = getResultSet().getObject(tcd.getName());
        if (tcd.getName().equals("CID"))
        {
          if (o instanceof Integer)
          {
            Integer i = (Integer)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(Integer)tcd.getValue(),i);
          }
          else
            fail("Type Integer expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CGEOMETRY") ||
          tcd.getName().equals("CPOINT") ||
          tcd.getName().equals("CLINESTRING") ||
          tcd.getName().equals("CPOLYGON") ||
          tcd.getName().equals("CMULTILINESTRING") ||
          tcd.getName().equals("CMULTIPOLYGON") ||
          tcd.getName().equals("CGEOMETRYCOLLECTION") ||
          tcd.getName().equals("CENUM") ||
          tcd.getName().equals("CSET"))
        {
          if (o instanceof String)
          {
            String s = (String)o;
            assertEquals("Invalid value for "+tcd.getType()+"!",(String)tcd.getValue(),s);
          }
          else
            fail("Type String expected for "+tcd.getType()+"!");
        }
        else if (tcd.getName().equals("CMULTIPOINT"))
        {
          if (o instanceof String)
          {
            String s = (String)o;
            String sExpected = (String)tcd.getValue();
            String sPrefix = "MULTIPOINT (";
            String sSuffix = ")";
            sExpected = sExpected.substring(sPrefix.length(),sExpected.length()-sSuffix.length());
            String[] as = sExpected.split(",");
            sExpected = sPrefix;
            for (int i = 0; i < as.length; i++)
            {
              if (i > 0)
                sExpected = sExpected + ", ";
              sExpected = sExpected + "("+as[i].trim()+")";
            }
            sExpected = sExpected + sSuffix;
            assertEquals("Invalid value for "+tcd.getType()+"!",sExpected,s);
          }
          else
            fail("Type String expected for "+tcd.getType()+"!");
        }
        else
          fail("Unexpected column: "+tcd.getName()+"!");
      }
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetObjectNativeComplex */

  @Override
  @Test
  public void testUpdateNull()
  {
    enter();
    try 
    { 
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
      getResultSet().updateNull("CCHAR_5");
    }
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateNull */
  
  @Override
  @Test
  public void testUpdateString()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CVARCHAR_255");
      getResultSet().updateString(tcd.getName(),(String)tcd.getValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateString */
  
  @Override
  @Test
  public void testUpdateNString()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CNVARCHAR_127");
      getResultSet().updateNString(tcd.getName(),(String)tcd.getValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateNString */
  
  @Override
  @Test
  public void testUpdateClob()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CCLOB_2M");
      Clob clob = getResultSet().getStatement().getConnection().createClob();
      clob.setString(1l,(String)tcd.getValue());
      getResultSet().updateClob(tcd.getName(),clob);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateClob */

  @Override
  @Test
  public void testUpdateClob_Reader()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CCLOB_2M");
      Reader rdr = new StringReader((String)tcd.getValue());
      getResultSet().updateClob(tcd.getName(),rdr);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateClob_Reader */
  
  @Override
  @Test
  public void testUpdateClob_Reader_Long()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CCLOB_2M");
      Reader rdr = new StringReader((String)tcd.getValue());
      getResultSet().updateClob(tcd.getName(),rdr,((String)tcd.getValue()).length());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateClob_Reader_Long */
  
  @Override
  @Test
  public void testUpdateNClob()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CNCLOB_1M");
      NClob nclob = getResultSet().getStatement().getConnection().createNClob();
      nclob.setString(1l, (String)tcd.getValue());
      getResultSet().updateNClob(tcd.getName(),nclob);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateNClob */
  
  @Override
  @Test
  public void testUpdateNClob_Reader()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CNCLOB_1M");
      Reader rdr = new StringReader((String)tcd.getValue());
      getResultSet().updateNClob(tcd.getName(),rdr);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateNClob_Reader */
  
  @Override
  @Test
  public void testUpdateNClob_Reader_Long()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CNCLOB_1M");
      Reader rdr = new StringReader((String)tcd.getValue());
      getResultSet().updateNClob(tcd.getName(),rdr,((String)tcd.getValue()).length());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateNClob_Reader_Long */
  
  
  @Override
  @Test
  public void testUpdateSqlXml()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CXML");
      SQLXML sqlxml = getResultSet().getStatement().getConnection().createSQLXML();
      sqlxml.setString((String)tcd.getValue());
      getResultSet().updateSQLXML(tcd.getName(),sqlxml);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateSqlXml */
  
  @Override
  @Test
  public void testUpdateBytes()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CVARBINARY_255");
      getResultSet().updateBytes(tcd.getName(),(byte[])tcd.getValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateBytes */
  
  @Override
  @Test
  public void testUpdateBlob()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CBLOB");
      Blob blob = getResultSet().getStatement().getConnection().createBlob();
      blob.setBytes(1, (byte[])tcd.getValue());
      getResultSet().updateBlob(tcd.getName(),blob);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateBlob */

  @Override
  @Test
  public void testUpdateBlob_InputStream()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CBLOB");
      InputStream is = new ByteArrayInputStream((byte[])tcd.getValue());
      getResultSet().updateBlob(tcd.getName(),is);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateBlob_InputStream */
  
  @Override
  @Test
  public void testUpdateBlob_InputStream_Long()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CBLOB");
      InputStream is = new ByteArrayInputStream((byte[])tcd.getValue());
      getResultSet().updateBlob(tcd.getName(),is,((byte[])tcd.getValue()).length);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateBlob_String_InputStream_Long */
  
  @Override
  @Test
  public void testUpdateBigDecimal()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CDECIMAL_15_5");
      getResultSet().updateBigDecimal(tcd.getName(),(BigDecimal)tcd.getValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateBigDecimal */
  
  @Override
  @Test
  public void testUpdateByte()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CBOOLEAN");
      getResultSet().updateByte(tcd.getName(),((Boolean)tcd.getValue()).booleanValue()?(byte)1:(byte)0);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateByte */
  
  @Override
  @Test
  public void testUpdateShort()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CSMALLINT");
      getResultSet().updateShort(tcd.getName(),((Short)tcd.getValue()).shortValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateShort */
  
  @Override
  @Test
  public void testUpdateInt()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CINTEGER");
      getResultSet().updateInt(tcd.getName(),((Integer)tcd.getValue()).intValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateInt */
  
  @Override
  @Test
  public void testUpdateLong()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(_listCdSimple,"CBIGINT");
      getResultSet().updateLong(tcd.getName(),((Long)tcd.getValue()).longValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateLong */
  
  @Override
  @Test
  public void testUpdateFloat()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CREAL");
      getResultSet().updateFloat(tcd.getName(),((Float)tcd.getValue()).floatValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateFloat */
  
  @Override
  @Test
  public void testUpdateDouble()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CDOUBLE");
      getResultSet().updateDouble(tcd.getName(),((Double)tcd.getValue()).doubleValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateDouble */
  
  @Override
  @Test
  public void testUpdateBoolean()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CBOOLEAN");
      getResultSet().updateBoolean(tcd.getName(),((Boolean)tcd.getValue()).booleanValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateBoolean */
  
  @Override
  @Test
  public void testUpdateDate()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CDATE");
      getResultSet().updateDate(tcd.getName(),(Date)tcd.getValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateDate */
  
  @Override
  @Test
  public void testUpdateTime()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CTIME");
      getResultSet().updateTime(tcd.getName(),(Time)tcd.getValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetTime */
  
  @Override
  @Test
  public void testUpdateTimestamp()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CTIMESTAMP");
      getResultSet().updateTimestamp(tcd.getName(),(Timestamp)tcd.getValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateTimestamp */

  @Test
  public void testUpdateDuration()
  {
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CINTERVAL_DAY_2_SECONDS_6");
      getBaseResultSet().updateDuration(tcd.getName(), ((Interval)tcd.getValue()).toDuration());
    }
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateDuration */
  
  @Override
  @Test
  public void testUpdateAsciiStream()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CVARCHAR_255");
      InputStream is = new ByteArrayInputStream(((String)tcd.getValue()).getBytes());
      getResultSet().updateAsciiStream(tcd.getName(),is);
    }
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateAsciiStream */
  
  @Override
  @Test
  public void testUpdateAsciiStream_Int()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CVARCHAR_255");
      InputStream is = new ByteArrayInputStream(((String)tcd.getValue()).getBytes());
      getResultSet().updateAsciiStream(tcd.getName(),is,((String)tcd.getValue()).length());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateAsciiStream_Int */
  
  @Override
  @Test
  public void testUpdateAsciiStream_Long()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CVARCHAR_255");
      InputStream is = new ByteArrayInputStream(((String)tcd.getValue()).getBytes());
      getResultSet().updateAsciiStream(tcd.getName(),is,(long)((String)tcd.getValue()).length());
    }
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateAsciiStream_Long */
  
  @Override
  @Test
  public void testUpdateCharacterStream()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CVARCHAR_255");
      Reader rdr = new StringReader((String)tcd.getValue());
      getResultSet().updateCharacterStream(tcd.getName(),rdr);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateCharacterStream */
  
  @Override
  @Test
  public void testUpdateCharacterStream_Int()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CVARCHAR_255");
      Reader rdr = new StringReader((String)tcd.getValue());
      getResultSet().updateCharacterStream(tcd.getName(),rdr,((String)tcd.getValue()).length());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateCharacterStream_Int */
  
  @Override
  @Test
  public void testUpdateCharacterStream_Long()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CVARCHAR_255");
      Reader rdr = new StringReader((String)tcd.getValue());
      getResultSet().updateCharacterStream(tcd.getName(),rdr,(long)((String)tcd.getValue()).length());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateCharacterStream_Long */
  
  @Override
  @Test
  public void testUpdateNCharacterStream()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CNVARCHAR_127");
      Reader rdr = new StringReader((String)tcd.getValue());
      getResultSet().updateCharacterStream(tcd.getName(),rdr);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateNCharacterStream */
  
  @Override
  @Test
  public void testUpdateNCharacterStream_Int()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CNVARCHAR_127");
      Reader rdr = new StringReader((String)tcd.getValue());
      getResultSet().updateCharacterStream(tcd.getName(),rdr,((String)tcd.getValue()).length());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateNCharacterStream_String_Int */
  
  @Override
  @Test
  public void testUpdateNCharacterStream_Long()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CNVARCHAR_127");
      Reader rdr = new StringReader((String)tcd.getValue());
      getResultSet().updateCharacterStream(tcd.getName(),rdr,(long)((String)tcd.getValue()).length());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateNCharacterStream_String_Long */

  @Override
  @Test
  public void testUpdateBinaryStream()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CVARBINARY_255");
      InputStream is = new ByteArrayInputStream((byte[])tcd.getValue());
      getResultSet().updateBinaryStream(tcd.getName(),is);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateBinaryStream */
  
  @Override
  @Test
  public void testUpdateBinaryStream_Int()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CVARBINARY_255");
      InputStream is = new ByteArrayInputStream((byte[])tcd.getValue());
      getResultSet().updateBinaryStream(tcd.getName(),is,((byte[])tcd.getValue()).length);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateBinaryStream_Int */
  
  @Override
  @Test
  public void testUpdateBinaryStream_Long()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CVARBINARY_255");
      InputStream is = new ByteArrayInputStream((byte[])tcd.getValue());
      getResultSet().updateBinaryStream(tcd.getName(),is,(long)((byte[])tcd.getValue()).length);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateBinaryStream_Long */
  
  @Override
  @Test
  public void testUpdateArray()
  {
    // no arrays in DB/2 tables
  } /* testUpdateArray */
  
  @Override
  @Test
  public void testUpdateRef()
  {
    // no REFs in DB/2 tables
  } /* testUpdateRef */
  
  @Override
  @Test
  public void testUpdateRowId()
  {
  } /* testUpdateRowId */
  
  @Override
  @Test
  public void testUpdateObject()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CDATE");
      getResultSet().updateObject(tcd.getName(),tcd.getValue());
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateObject */
  
  @Override
  @Test
  public void testUpdateObject_Int()
  {
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CDECIMAL_15_5");
      getResultSet().updateObject(tcd.getName(),tcd.getValue(),3);
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testUpdateObject_Int */
  
  @Override
  @Test
  public void testRefreshRow() throws SQLException
  {
    enter();
    try 
    { 
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().refreshRow(); 
    }
    catch(SQLException se) { System.out.println(EU.getExceptionMessage(se)); }
  } /* refreshRow */

  @Override
  @Test
  public void testDeleteRow() throws SQLException
  {
    try 
    {
      // cannot delete row in simple table because of foreign key
      openResultSet(_sSqlQueryComplex,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
      // cannot delete row in complex table because of a bug in JDBC driver
      // probably the result set is in an ArrayList which is based on an array
      // and therefore cannot be removed.
      getResultSet().deleteRow();
      // restore the database
      tearDown();
      setUpClass();
      setUp();
    }
    catch(Exception e) { System.out.println(EU.getExceptionMessage(e)); }
  } /* testDeleteRow */

  @Override
  @Test
  public void testRowDeleted()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQueryComplex,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
    	  getResultSet().rowDeleted();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testRowDeleted */

  
  @Override
  @Test
  public void testUpdateRow() throws SQLException
  {
    try 
    { 
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CXML");
      SQLXML sqlxml = getResultSet().getStatement().getConnection().createSQLXML();
      sqlxml.setString("<a>Konrad Zuse</a>");
      getResultSet().updateSQLXML(tcd.getName(), sqlxml);
      getResultSet().updateRow(); 
      sqlxml.free();
      // restore the database
      tearDown();
      setUpClass();
      setUp();
    }
    catch(Exception e) { fail(EU.getExceptionMessage(e)); }
  } /* testUpdateRow */

  @Override
  @Test
  public void testRowUpdated()
  {
    enter();
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
      getResultSet().rowUpdated(); 
    }
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testRowUpdated */
  
  @Override
  @Test
  public void testInsertRow() throws SQLException
  {
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
      getResultSet().moveToInsertRow();
      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CINTEGER");
      getResultSet().updateInt(tcd.getName(),((Integer)tcd.getValue()).intValue());
      getResultSet().insertRow();
      // restore the database
      tearDown();
      setUpClass();
      setUp();
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testInsertRow */

  @Override
  @Test
  public void testRowInserted()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
    	  getResultSet().rowInserted();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testRowInserted */
  
  
  @Test
  public void testInsertRowSimple() throws SQLException
  {
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      getResultSet().moveToInsertRow();
      TestColumnDefinition tcd = findColumnDefinition(_listCdSimple,"CCHAR_5");
      getResultSet().updateString(tcd.getName(),(String)tcd.getValue());
      tcd = findColumnDefinition(_listCdSimple,"CVARCHAR_255");
      getResultSet().updateString(tcd.getName(),(String)tcd.getValue());
      tcd = findColumnDefinition(_listCdSimple,"CCLOB_2M");
      Clob clob = getResultSet().getStatement().getConnection().createClob();
      clob.setString(1l, (String)tcd.getValue());
      getResultSet().updateClob(tcd.getName(), clob);
      tcd = findColumnDefinition(_listCdSimple,"CNCHAR_5");
      getResultSet().updateString(tcd.getName(),(String)tcd.getValue());
      tcd = findColumnDefinition(_listCdSimple,"CNVARCHAR_127");
      getResultSet().updateString(tcd.getName(),(String)tcd.getValue());
      tcd = findColumnDefinition(_listCdSimple,"CNCLOB_1M");
      NClob nclob = getResultSet().getStatement().getConnection().createNClob();
      nclob.setString(1l, (String)tcd.getValue());
      getResultSet().updateNClob(tcd.getName(), nclob);
      tcd = findColumnDefinition(_listCdSimple,"CXML");
      SQLXML sqlxml = getResultSet().getStatement().getConnection().createSQLXML();
      sqlxml.setString((String)tcd.getValue());
      getResultSet().updateSQLXML(tcd.getName(), sqlxml);
      tcd = findColumnDefinition(_listCdSimple,"CBINARY_5");
      getResultSet().updateBytes(tcd.getName(),(byte[])tcd.getValue());
      tcd = findColumnDefinition(_listCdSimple,"CVARBINARY_255");
      getResultSet().updateBytes(tcd.getName(),(byte[])tcd.getValue());
      tcd = findColumnDefinition(_listCdSimple,"CBLOB");
      Blob blob = getResultSet().getStatement().getConnection().createBlob();
      blob.setBytes(1l, (byte[])tcd.getValue());
      getResultSet().updateBlob(tcd.getName(), blob);
      tcd = findColumnDefinition(_listCdSimple,"CNUMERIC_31");
      getResultSet().updateBigDecimal(tcd.getName(),new BigDecimal((BigInteger)tcd.getValue()));
      tcd = findColumnDefinition(_listCdSimple,"CDECIMAL_15_5");
      getResultSet().updateBigDecimal(tcd.getName(),(BigDecimal)tcd.getValue());
      tcd = findColumnDefinition(_listCdSimple,"CSMALLINT");
      getResultSet().updateShort(tcd.getName(),((Short)tcd.getValue()).shortValue());
      tcd = findColumnDefinition(_listCdSimple,"CINTEGER");
      getResultSet().updateInt(tcd.getName(),((Integer)tcd.getValue()).intValue());
      tcd = findColumnDefinition(_listCdSimple,"CBIGINT");
      getResultSet().updateLong(tcd.getName(),((Long)tcd.getValue()).longValue());
      tcd = findColumnDefinition(_listCdSimple,"CFLOAT_10");
      getResultSet().updateFloat(tcd.getName(),((Float)tcd.getValue()).floatValue());
      tcd = findColumnDefinition(_listCdSimple,"CREAL");
      getResultSet().updateFloat(tcd.getName(),((Float)tcd.getValue()).floatValue());
      tcd = findColumnDefinition(_listCdSimple,"CDOUBLE");
      getResultSet().updateDouble(tcd.getName(),((Double)tcd.getValue()).doubleValue());
      tcd = findColumnDefinition(_listCdSimple,"CBOOLEAN");
      getResultSet().updateBoolean(tcd.getName(),((Boolean)tcd.getValue()).booleanValue());
      tcd = findColumnDefinition(_listCdSimple,"CDATE");
      getResultSet().updateDate(tcd.getName(),(Date)tcd.getValue());
      tcd = findColumnDefinition(_listCdSimple,"CTIME");
      getResultSet().updateTime(tcd.getName(),(Time)tcd.getValue());
      tcd = findColumnDefinition(_listCdSimple,"CTIMESTAMP");
      getResultSet().updateTimestamp(tcd.getName(),(Timestamp)tcd.getValue());
      tcd = findColumnDefinition(_listCdSimple,"CINTERVAL_YEAR_3_MONTH");
      ((BaseResultSet)getResultSet()).updateDuration(tcd.getName(),((Interval)tcd.getValue()).toDuration());
      tcd = findColumnDefinition(_listCdSimple,"CINTERVAL_DAY_2_SECONDS_6");
      ((BaseResultSet)getResultSet()).updateDuration(tcd.getName(),((Interval)tcd.getValue()).toDuration());
      getResultSet().insertRow();
      getResultSet().moveToCurrentRow();
      
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      tcd = findColumnDefinition(
        _listCdSimple,"CINTEGER");
      while ((getResultSet().getInt(tcd.getName()) != ((Integer)tcd.getValue()).intValue()) && 
        getResultSet().next()) {}
      tcd = findColumnDefinition(_listCdSimple,"CCHAR_5");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (String)tcd.getValue(),
        (getResultSet().getString(tcd.getName())).substring(0,((String)tcd.getValue()).length()));
      tcd = findColumnDefinition(_listCdSimple,"CVARCHAR_255");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (String)tcd.getValue(),
        getResultSet().getString(tcd.getName()));
      tcd = findColumnDefinition(_listCdSimple,"CCLOB_2M");
      clob = getResultSet().getClob(tcd.getName());
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (String)tcd.getValue(),
        clob.getSubString(1l, (int)clob.length()));
      tcd = findColumnDefinition(_listCdSimple,"CNCHAR_5");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (String)tcd.getValue(),
        (getResultSet().getString(tcd.getName())).substring(0,((String)tcd.getValue()).length()));
      tcd = findColumnDefinition(_listCdSimple,"CNVARCHAR_127");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (String)tcd.getValue(),
        getResultSet().getString(tcd.getName()));
      tcd = findColumnDefinition(_listCdSimple,"CNCLOB_1M");
      nclob = getResultSet().getNClob(tcd.getName());
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (String)tcd.getValue(),
        nclob.getSubString(1l, (int)nclob.length()));
      tcd = findColumnDefinition(_listCdSimple,"CXML");
      sqlxml = getResultSet().getSQLXML(tcd.getName());
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (String)tcd.getValue(),
        sqlxml.getString());
      tcd = findColumnDefinition(_listCdSimple,"CBINARY_5");
      assertTrue("Insert of "+tcd.getType()+" failed!",
        Arrays.equals(
          (byte[])tcd.getValue(),
          Arrays.copyOf(
            getResultSet().getBytes(tcd.getName()),
            ((byte[])tcd.getValue()).length)));
      tcd = findColumnDefinition(_listCdSimple,"CVARBINARY_255");
      assertTrue("Insert of "+tcd.getType()+" failed!",
        Arrays.equals(
          (byte[])tcd.getValue(),
          getResultSet().getBytes(tcd.getName())));
      tcd = findColumnDefinition(_listCdSimple,"CBLOB");
      blob = getResultSet().getBlob(tcd.getName());
      assertTrue("Insert of "+tcd.getType()+" failed!",
        Arrays.equals(
          (byte[])tcd.getValue(),
          blob.getBytes(1l,(int)blob.length())));
      tcd = findColumnDefinition(_listCdSimple,"CNUMERIC_31");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (BigInteger)tcd.getValue(),
        getResultSet().getBigDecimal(tcd.getName()).toBigInteger());
      tcd = findColumnDefinition(_listCdSimple,"CDECIMAL_15_5");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (BigDecimal)tcd.getValue(),
        getResultSet().getBigDecimal(tcd.getName()));
      tcd = findColumnDefinition(_listCdSimple,"CSMALLINT");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        ((Short)tcd.getValue()).shortValue(),
        getResultSet().getShort(tcd.getName()));
      tcd = findColumnDefinition(_listCdSimple,"CINTEGER");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        ((Integer)tcd.getValue()).intValue(),
        getResultSet().getInt(tcd.getName()));
      tcd = findColumnDefinition(_listCdSimple,"CBIGINT");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        ((Long)tcd.getValue()).longValue(),
        getResultSet().getLong(tcd.getName()));
      tcd = findColumnDefinition(_listCdSimple,"CFLOAT_10");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (Float)tcd.getValue(),
        Float.valueOf(getResultSet().getFloat(tcd.getName())),0.00001);
      tcd = findColumnDefinition(_listCdSimple,"CREAL");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (Float)tcd.getValue(),
        Float.valueOf(getResultSet().getFloat(tcd.getName())),0.00001);
      tcd = findColumnDefinition(_listCdSimple,"CDOUBLE");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (Double)tcd.getValue(),
        Double.valueOf(getResultSet().getDouble(tcd.getName())));
      tcd = findColumnDefinition(_listCdSimple,"CBOOLEAN");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (Boolean)tcd.getValue(),
        Boolean.valueOf(getResultSet().getBoolean(tcd.getName())));
      tcd = findColumnDefinition(_listCdSimple,"CDATE");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (Date)tcd.getValue(),
        getResultSet().getDate(tcd.getName()));
      tcd = findColumnDefinition(_listCdSimple,"CTIME");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (Time)tcd.getValue(),
        getResultSet().getTime(tcd.getName()));
      tcd = findColumnDefinition(_listCdSimple,"CTIMESTAMP");
      Timestamp tsExpected = (Timestamp)tcd.getValue();
      tsExpected.setNanos(1000*((tsExpected.getNanos()+499)/1000));
      assertEquals("Insert of "+tcd.getType()+" failed!",
        tsExpected,
        getResultSet().getTimestamp(tcd.getName()));
      tcd = findColumnDefinition(_listCdSimple,"CINTERVAL_YEAR_3_MONTH");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        (Interval)tcd.getValue(),
        Interval.fromDuration(((BaseResultSet)getResultSet()).getDuration(tcd.getName())));
      tcd = findColumnDefinition(_listCdSimple,"CINTERVAL_DAY_2_SECONDS_6");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        ((Interval)tcd.getValue()).toDuration(),
        ((BaseResultSet)getResultSet()).getDuration(tcd.getName()));
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
    finally {
      // restore the database
      tearDown();
      setUpClass();
      setUp();
    }
  } /* testInsertRowSimple */
  
  @Test
  public void testInsertRowComplex() throws SQLException
  {
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      // needed for foreign key constraint ...
      getResultSet().moveToInsertRow();
      TestColumnDefinition tcd = findColumnDefinition(
        _listCdSimple,"CINTEGER");
      getResultSet().updateInt(tcd.getName(),((Integer)tcd.getValue()).intValue());
      getResultSet().insertRow();
      getResultSet().moveToCurrentRow();

      openResultSet(_sSqlQueryComplex,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
      getResultSet().moveToInsertRow();
      tcd = findColumnDefinition(_listCdComplex,"CID");
      getResultSet().updateInt(tcd.getName(),(((Integer)tcd.getValue())).intValue());
      
      getResultSet().insertRow();
      getResultSet().moveToCurrentRow();
      
      openResultSet(_sSqlQueryComplex,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      tcd = findColumnDefinition(_listCdComplex,"CID");
      while ((getResultSet().getInt(tcd.getName()) != ((Integer)tcd.getValue()).intValue()) && 
        getResultSet().next()) {}

      tcd = findColumnDefinition(_listCdComplex,"CID");
      assertEquals("Insert of "+tcd.getType()+" failed!",
        ((Integer)tcd.getValue()).intValue(),
        getResultSet().getInt(tcd.getName()));
      // restore the database
      tearDown();
      setUpClass();
      setUp();
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testInsertRowComplex */
  
  @Override
  @Test
  public void testMoveToInsertRow() throws SQLException
  {
    enter();
    try 
    { 
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      getResultSet().moveToInsertRow();
    }
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testMoveToInsertRow */

  @Override
  @Test
  public void testMoveToCurrentRow() throws SQLException
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

      getResultSet().moveToCurrentRow(); 
    }
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testMoveToCurrentRow */
  
  @Override
  @Test
  public void testCancelRowUpdates() throws SQLException
  {
    enter();
    try 
    { 
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
      getResultSet().cancelRowUpdates(); 
    }
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
  } /* testCancelRowUpdates */

  @Override
  @Test
  public void testAbsolute()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
    	  getResultSet().absolute(1); 
    	}
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { System.out.println(EU.getExceptionMessage(se)); }
  } /* testAbsolute */
  
  @Override
  @Test
  public void testRelative()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
    	  getResultSet().relative(1);
    	}
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { System.out.println(EU.getExceptionMessage(se)); }
  } /* testRelative */
  
  @Override
  @Test
  public void testBeforeFirst()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
    	  getResultSet().beforeFirst(); 
    	}
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { System.out.println(EU.getExceptionMessage(se)); }
  } /* testBeforeFirst */
  
  @Override
  @Test
  public void testAfterLast()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
      	getResultSet().afterLast(); 
    	}
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { System.out.println(EU.getExceptionMessage(se)); }
  } /* testAfterLast */
  
  @Override
  @Test
  public void testPrevious()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
      getResultSet().next();
      getResultSet().previous();
    }
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { System.out.println(EU.getExceptionMessage(se)); }
  } /* testPrevious */
  
  @Override
  @Test
  public void testFirst()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
      	getResultSet().first();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { System.out.println(EU.getExceptionMessage(se)); }
  } /* testFirst */
  
  @Override
  @Test
  public void testLast()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
      	getResultSet().last();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { System.out.println(EU.getExceptionMessage(se)); }
  } /* testLast */
  
  @Override
  @Test
  public void testNext()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
    	  getResultSet().next();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { System.out.println(EU.getExceptionMessage(sfnse)); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testNext */
  
  @Override
  @Test
  public void testGetStatement()
  {
    enter();
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
    	  getResultSet().getStatement();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetStatement */

  @Override
  @Test
  public void testGetWarnings()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().getWarnings();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetWarnings */
  
  @Override
  @Test
  public void testClearWarnings()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().clearWarnings();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testClearWarnings */
  
  @Override
  @Test
  public void testGetCursorName()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().getCursorName(); 
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetCursorName */
  
  @Override
  @Test
  public void testClose()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      Statement stmt = getResultSet().getStatement();
      getResultSet().close();
      if (!stmt.isClosed())
      	  stmt.close();
      if (!_conn.isClosed())
        _conn.rollback();
    }
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testClose */
  
  @Test
  public void testIsClosed()
  {
    enter();
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().isClosed();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testIsClosed */
  

  @Override
  @Test
  public void testGetHoldability()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().getHoldability();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetHoldability */
  
  @Override
  @Test
  public void testSetFetchDirection()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().setFetchDirection(ResultSet.FETCH_UNKNOWN);
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testSetFetchDirection */
  
  @Override
  @Test
  public void testGetFetchDirection()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().getFetchDirection();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetFetchDirection */
  
  @Override
  @Test
  public void testSetFetchSize()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().setFetchSize(20);
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testSetFetchSize */
  
  @Override
  @Test
  public void testGetFetchSize()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().getFetchSize();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetFetchSize */
  
  @Override
  @Test
  public void testGetType()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      assertEquals("Invalid result set type!",ResultSet.TYPE_FORWARD_ONLY,getResultSet().getType());
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetType */
  
  @Override
  @Test
  public void testGetConcurrency()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      assertEquals("Invalid result set concurrency!",ResultSet.CONCUR_READ_ONLY,getResultSet().getConcurrency());
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetConcurrency */
  
  @Override
  @Test
  public void testGetMetaData()
  {
    enter();
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().getMetaData(); 
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetMetaData */
  
  @Override
  @Test
  public void testGetRow()
  {
    enter();
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().getRow();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testGetRow */
  
  @Override
  @Test
  public void testIsBeforeFirst()
  {
    enter();
    try
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().isBeforeFirst();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testIsBeforeFirst */
  
  @Override
  @Test
  public void testIsAfterLast()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().isAfterLast();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testIsAfterLast */
  
  @Override
  @Test
  public void testIsFirst()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().isFirst();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testIsFirst */
  
  @Override
  @Test
  public void testIsLast()
  {
    enter();
    try 
    {
      openResultSet(_sSqlQuerySimple,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      getResultSet().isLast();
    	}
    catch(SQLFeatureNotSupportedException sfnse) { printExceptionMessage(sfnse); }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
  } /* testIsLast */

}
