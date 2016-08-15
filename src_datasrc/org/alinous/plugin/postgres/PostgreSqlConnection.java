package org.alinous.plugin.postgres;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
import org.alinous.exec.pages.PostContext;
import org.alinous.plugin.precompile.PreCompileValue;
import org.alinous.plugin.precompile.PreCompileValueBag;
import org.alinous.script.ISQLSentence;


public class PostgreSqlConnection implements Connection
{
	private Connection con;
	
	private int clearCount = 0;
	
	private Map<ISQLSentence, PreparedStatement> sqlStatements = new IdentityHashMap<ISQLSentence, PreparedStatement>();
	private Map<ISQLSentence, PreCompileValueBag> variavlePathes = new IdentityHashMap<ISQLSentence, PreCompileValueBag>();
	
	private boolean resultUpper;

	private AlinousCore core;
	
	public PostgreSqlConnection(AlinousCore core, Connection con, boolean resultUpper)
	{
		this.con = con;
		this.clearCount = 0;
		this.resultUpper = true;
		this.core = core;
	}
	
	public PreparedStatement getPreparedStatement(ISQLSentence select)
	{
		if(select == null){
			return null;
		}
		return this.sqlStatements.get(select);
	}
	
	public void clearPreparedStatement(ISQLSentence select)
	{
		if(select != null){
			this.sqlStatements.remove(select);
		}
	}
	
	public List<PreCompileValue> getListPreCompileValue(ISQLSentence select)
	{
		if(select == null){
			return null;
		}
		
		PreCompileValueBag bag = this.variavlePathes.get(select);
		if(bag == null){
			return null;
		}
		
		return bag.getList();
	}
	
	public void clearCachedByCount(int count){
		if(count < this.clearCount){
			clearCachedSQL();
			this.clearCount = 0;
			return;
		}
		
		this.clearCount++;
	}
	
	public void clearCachedSQL()
	{
		Iterator<ISQLSentence> it = this.sqlStatements.keySet().iterator();
		while(it.hasNext()){
			ISQLSentence key = it.next();
			
			try {
				this.sqlStatements.get(key).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		this.sqlStatements.clear();
		
		this.variavlePathes.clear();
	}
	
	public void closePrecompiledStatements()
	{
		Iterator<ISQLSentence> it = this.sqlStatements.keySet().iterator();
		while(it.hasNext()){
			ISQLSentence key = it.next();
			
			try {
				this.sqlStatements.get(key).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void clearWarnings() throws SQLException
	{
		this.con.clearWarnings();
	}

	public void close() throws SQLException
	{
		clearCachedSQL();
		
		this.con.close();		
	}

	public void commit() throws SQLException
	{
		this.con.commit();
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException
	{
		return this.con.createArrayOf(typeName, elements);
	}

	public Blob createBlob() throws SQLException
	{
		return this.con.createBlob();
	}

	public Clob createClob() throws SQLException
	{
		return this.con.createClob();
	}

	public NClob createNClob() throws SQLException
	{
		return this.con.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException
	{
		return this.con.createSQLXML();
	}

	public Statement createStatement() throws SQLException
	{
		return this.con.createStatement();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException
	{
		return this.con.createStatement();
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException
	{
		return this.con.createStatement(resultSetType, resultSetConcurrency, resultSetConcurrency);
	}

	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException
	{
		return this.con.createStruct(typeName, attributes);
	}

	public boolean getAutoCommit() throws SQLException
	{
		return this.con.getAutoCommit();
	}

	public String getCatalog() throws SQLException
	{
		return this.con.getCatalog();
	}

	public Properties getClientInfo() throws SQLException
	{
		return this.con.getClientInfo();
	}

	public String getClientInfo(String name) throws SQLException
	{
		return this.con.getClientInfo(name);
	}

	public int getHoldability() throws SQLException
	{
		return this.con.getHoldability();
	}

	public DatabaseMetaData getMetaData() throws SQLException
	{
		return this.con.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException
	{
		return this.con.getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException
	{
		return this.con.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException
	{
		return this.con.getWarnings();
	}

	public boolean isClosed() throws SQLException
	{
		return this.con.isClosed();
	}

	public boolean isReadOnly() throws SQLException
	{
		return this.con.isReadOnly();
	}

	public boolean isValid(int timeout) throws SQLException
	{
		return this.con.isValid(timeout);
	}

	public String nativeSQL(String sql) throws SQLException
	{
		return this.con.nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException
	{
		return this.con.prepareCall(sql);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException
	{
		return this.con.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException
	{
		return this.con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException
	{
		return this.con.prepareStatement(sql);
	}
	
	public PreparedStatement prepareStatement(String sql, PostContext context) throws SQLException
	{
		PreparedStatement stmt = this.con.prepareStatement(sql);
		
		stmt.setPoolable(true);
		if(stmt.isPoolable()){
			this.sqlStatements.put(context.getPrecompile().getSqlSentence(), stmt);
			this.variavlePathes.put(context.getPrecompile().getSqlSentence(), new PreCompileValueBag(context.getPrecompile().getPrecompileValues()));
		}
		
		return stmt;
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException
	{
		return this.con.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException
	{
		return this.con.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException
	{
		return this.con.prepareStatement(sql);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException
	{
		return this.con.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException
	{
		return this.con.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException
	{
		this.con.releaseSavepoint(savepoint);
	}

	public void rollback() throws SQLException
	{
		this.con.rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException
	{
		this.con.rollback(savepoint);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException
	{
		this.con.setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException
	{
		this.con.setCatalog(catalog);
	}

	public void setClientInfo(Properties properties)
			throws SQLClientInfoException
	{
		this.con.setClientInfo(properties);
	}

	public void setClientInfo(String name, String value)
			throws SQLClientInfoException
	{
		this.con.setClientInfo(name, value);
	}

	public void setHoldability(int holdability) throws SQLException
	{
		this.con.setHoldability(holdability);
	}

	public void setReadOnly(boolean readOnly) throws SQLException
	{
		this.con.setReadOnly(readOnly);
	}

	public Savepoint setSavepoint() throws SQLException
	{
		return this.con.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException
	{
		return this.con.setSavepoint();
	}

	public void setTransactionIsolation(int level) throws SQLException
	{
		this.con.setTransactionIsolation(level);
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException
	{
		this.con.setTypeMap(map);
	}

	public boolean isWrapperFor(Class<?> arg0) throws SQLException
	{
		return this.con.isWrapperFor(arg0);
	}

	public <T> T unwrap(Class<T> arg0) throws SQLException
	{
		return this.unwrap(arg0);
	}
	
	public int getKnouledge()
	{
		return this.sqlStatements.size();
	}

	public Connection getCon()
	{
		return con;
	}

	public void abort(Executor executor) throws SQLException {
		con.abort(executor);
	}

	public int getNetworkTimeout() throws SQLException {
		return con.getNetworkTimeout();
	}

	public String getSchema() throws SQLException {
		return con.getSchema();
	}

	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		con.setNetworkTimeout(executor, milliseconds);
	}

	public void setSchema(String schema) throws SQLException {
		con.setSchema(schema);
	}
	
	public boolean prepareToransaction(String trxIdentifier) throws SQLException
	{
		AlinousDebug.debugOut(core, "PREPARE TRANSACTION " + trxIdentifier);
		
		Statement stmt = con.createStatement();
		try{
			stmt.executeUpdate("PREPARE TRANSACTION '" + trxIdentifier + "'");
		}
		finally{
			stmt.close();
		}
		
		return true;
	}
	
	public void commitPrepared(String trxIdentifier) throws SQLException
	{
		AlinousDebug.debugOut(core, "COMMIT PREPARED " + trxIdentifier);
		
		Statement stmt = con.createStatement();
		
		try{
			stmt.executeUpdate("COMMIT PREPARED '" + trxIdentifier + "'");
		}finally{
			stmt.close();
		}
	}
	
	public void rollbackPrepared(String trxIdentifier) throws SQLException
	{
		AlinousDebug.debugOut(core, "ROLLBACK PREPARED " + trxIdentifier);
		
		Statement stmt = con.createStatement();
		
		try{
			stmt.executeUpdate("ROLLBACK PREPARED '" + trxIdentifier + "'");
		}finally{
			stmt.close();
		}
	}

	public boolean isResultUpper() {
		return resultUpper;
	}

	public void setResultUpper(boolean resultUpper) {
		this.resultUpper = resultUpper;
	}
	
}
