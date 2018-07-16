package ch.admin.bar.siard2.mysql;

import java.io.*;
import java.sql.*;
import java.util.*;
import static org.junit.Assert.*;
import ch.admin.bar.siard2.jdbc.*;
import ch.enterag.utils.*;
import ch.enterag.utils.base.*;
import ch.enterag.sqlparser.*;
import ch.enterag.sqlparser.identifier.*;

public class TestBlobDatabase 
{
  public static final String _sTEST_SCHEMA = "TESTBLOBSCHEMA";
  private static final String _sTEST_TABLE_SIMPLE = "TBLOBSIMPLE";
  public static QualifiedId getQualifiedSimpleTable() { return new QualifiedId(null,_sTEST_SCHEMA,_sTEST_TABLE_SIMPLE); }

  public static int _iPrimarySimple = -1;
  private static List<TestColumnDefinition> getCdSimple() 
  {
    List<TestColumnDefinition> listCdSimple = new ArrayList<TestColumnDefinition>();
    
    // Numeric Data Types: Integer Types (Exact Values)
    _iPrimarySimple = listCdSimple.size(); // next column will be primary key column 
    listCdSimple.add(new TestColumnDefinition("CINT","INT",Integer.valueOf(0)));

    // PNG
    listCdSimple.add(new TestColumnDefinition("CPNG","LONGBLOB",TestUtils.getBytes(1)));

    // FLAC
    listCdSimple.add(new TestColumnDefinition("CFLAC","LONGBLOB",TestUtils.getBytes(1)));

    return listCdSimple;    
  }
  public static List<TestColumnDefinition> _listCdSimple = getCdSimple();
  
	private Connection _conn;
	private List<String> _listPngs = null;
	private List<String> _listFlacs = null;

	public TestBlobDatabase(MySqlConnection connMySql,List<String> listPngs, List<String> listFlacs)
	  throws SQLException, IOException
	{
	  _listPngs = listPngs;
	  _listFlacs = listFlacs;
		_conn = connMySql.unwrap(Connection.class);
		_conn.setAutoCommit(false);
		drop();
		create();
	} /* constructor */

  private void drop()
  {
    deleteTables();
    dropTables();
    dropSchema();
  } /* drop */
  
  private void executeDrop(String sSql)
  {
    try
    {
      Statement stmt = _conn.createStatement();
      stmt.executeUpdate(sSql);
      stmt.close();
      _conn.commit();
    }
    catch(SQLException se) { System.out.println(EU.getExceptionMessage(se)); }
  } /* executeDrop */

  private void deleteTables()
  {
    deleteTable(getQualifiedSimpleTable());
  } /* deleteTables */
  
  private void deleteTable(QualifiedId qiTable)
  {
    executeDrop("DELETE FROM "+qiTable.format());
  } /* deleteTable */
  
  private void dropTables()
  {
    dropTable(getQualifiedSimpleTable());
  } /* dropTables */
  
  private void dropTable(QualifiedId qiTable)
  {
    executeDrop("DROP TABLE "+qiTable.format());
  } /* dropTable */

  private void dropSchema()
  {
    executeDrop("DROP SCHEMA "+SqlLiterals.formatId(_sTEST_SCHEMA));
  } /* dropSchema */

  private void create()
    throws SQLException, IOException
  {
    createSchema();
    createTables();
    insertTables();
  } /* create */

  private void executeCreate(String sSql)
    throws SQLException
  {
    Statement stmt = _conn.createStatement();
    stmt.executeUpdate(sSql);
    stmt.close();
    _conn.commit();
  } /* executeCreate */

  private void createSchema()
    throws SQLException
  {
    SchemaId sid = new SchemaId(null,_sTEST_SCHEMA);
    executeCreate("CREATE SCHEMA "+sid.format());
  } /* createSchema */
  
  private void createTables()
    throws SQLException
  {
    createTable(getQualifiedSimpleTable(),_listCdSimple,
      Arrays.asList(new String[] {_listCdSimple.get(_iPrimarySimple).getName()}));
  } /* createTables */
  
  private void createTable(QualifiedId qiTable, List<TestColumnDefinition> listCd,
    List<String> listPrimary)
    throws SQLException
  {
    StringBuilder sbSql = new StringBuilder("CREATE TABLE ");
    sbSql.append(qiTable.format());
    sbSql.append("\r\n(\r\n  ");
    for (int iColumn = 0; iColumn < listCd.size(); iColumn++)
    {
      TestColumnDefinition tcd = listCd.get(iColumn); 
      if (iColumn > 0)
        sbSql.append(",\r\n  ");
      sbSql.append(tcd.getName());
      sbSql.append(" ");
      sbSql.append(tcd.getType());
    }
    if (listPrimary != null)
    {
      sbSql.append(",\r\n  CONSTRAINT PK");
      sbSql.append(qiTable.getName());
      sbSql.append(" PRIMARY KEY(");
      sbSql.append(SqlLiterals.formatIdentifierCommaList(listPrimary));
      sbSql.append(")");
    }
    sbSql.append("\r\n)");
    executeCreate(sbSql.toString());
  } /* createTable */
  
  private void insertTables()
    throws SQLException, IOException
  {
    for (int iRecord = 0; (iRecord < _listPngs.size()) && (iRecord < _listFlacs.size()); iRecord++)
      insertTableRecord(getQualifiedSimpleTable(),_listCdSimple,iRecord);
  } /* insertTables */
  
  private void insertTableRecord(QualifiedId qiTable, List<TestColumnDefinition> listCd, int iRecord)
    throws SQLException, IOException
  {
    System.out.println("Record: "+String.valueOf(iRecord));
    StringBuilder sbSql = new StringBuilder("INSERT INTO ");
    sbSql.append(qiTable.format());
    sbSql.append("\r\n(\r\n  ");
    for (int iColumn = 0; iColumn < listCd.size(); iColumn++)
    {
      TestColumnDefinition tcd = listCd.get(iColumn); 
      if (tcd.getValue() != null)
      {
        if (iColumn > 0)
          sbSql.append(",\r\n  ");
        sbSql.append(tcd.getName());
      }
    }
    sbSql.append("\r\n)\r\nVALUES\r\n(\r\n  ");
    List<Object> listLobs = new ArrayList<Object>();
    for (int iColumn = 0; iColumn < listCd.size(); iColumn++)
    {
      TestColumnDefinition tcd = listCd.get(iColumn);
      if (tcd.getValue() != null)
      {
        if (iColumn > 0)
          sbSql.append(",\r\n  ");
        if (iColumn == 0)
          sbSql.append(Integer.valueOf(iRecord).toString());
        else
        {
          sbSql.append("?");
          listLobs.add(tcd.getValue());
        }
      }
    }
    sbSql.append("\r\n)");
    PreparedStatement pstmt = _conn.prepareStatement(sbSql.toString());
    for (int iLob = 0; iLob < listLobs.size(); iLob++)
    {
      InputStream isBlob = null;
      if (iLob == 0)
        isBlob = new FileInputStream(_listPngs.get(iRecord));
      else
        isBlob = new FileInputStream(_listFlacs.get(iRecord));
      pstmt.setBinaryStream(iLob+1, isBlob);
    }
    int iResult = pstmt.executeUpdate();
    assertSame("Insert failed!",1,iResult);
    pstmt.close();
    _conn.commit();
  } /* insertTable */

} /* TestBlobDatabase */
