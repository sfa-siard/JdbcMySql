package ch.admin.bar.siard2.jdbc;

import static org.junit.Assert.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.junit.*;

import ch.admin.bar.siard2.jdbcx.*;
import ch.admin.bar.siard2.mysql.*;
import ch.enterag.utils.*;
import ch.enterag.utils.base.*;

public class MySqlBlobTester
{
  private static final ConnectionProperties _cp = new ConnectionProperties();
  private static final String _sDB_URL = MySqlDriver.getUrl(_cp.getHost() + ":" + _cp.getPort()+"/"+_cp.getCatalog(),true);
  private static final String _sDB_USER = _cp.getUser();
  private static final String _sDBA_USER = _cp.getDbaUser();
  private static final String _sDBA_PASSWORD = _cp.getDbaPassword();
  
  private List<String> _listPngs = new ArrayList<String>();
  private List<String> _listFlacs = new ArrayList<String>();

  @Test
  public void test() 
  {
    try
    {
      _listPngs.clear();
      for (int iRecord = 0; _cp.getBlobPng(iRecord) != null; iRecord++)
        _listPngs.add(_cp.getBlobPng(iRecord));
      _listFlacs.clear();
      for (int iRecord = 0; _cp.getBlobFlac(iRecord) != null; iRecord++)
        _listFlacs.add(_cp.getBlobFlac(iRecord));
      MySqlDataSource dsMySql = new MySqlDataSource();
      dsMySql.setUrl(_sDB_URL);
      dsMySql.setUser(_sDBA_USER);
      dsMySql.setPassword(_sDBA_PASSWORD);
      MySqlConnection connMySql = (MySqlConnection) dsMySql.getConnection();
      new TestBlobDatabase(connMySql,_listPngs,_listFlacs);
      TestMySqlDatabase.grantSchemaUser(connMySql, 
        TestBlobDatabase._sTEST_SCHEMA, _sDB_USER);
      connMySql.close();
    }
    catch(SQLException se) { fail(EU.getExceptionMessage(se)); }
    catch(IOException ie) { fail(EU.getExceptionMessage(ie)); }
  }

}
