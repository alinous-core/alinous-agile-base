/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alinous.plugin.postgres;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileOutputStream;
import org.alinous.datasrc.IAlinousDataSource;
import org.alinous.datasrc.basic.ILogProvidor;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.DataField;
import org.alinous.datasrc.types.DataTable;
import org.alinous.datasrc.types.PlSQLFunction;
import org.alinous.datasrc.types.PlSQLTrigger;
import org.alinous.datasrc.types.Record;
import org.alinous.datasrc.types.TypeHelper;
import org.alinous.datasrc.types.UpdateType;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.plugin.precompile.PreCompileValue;
import org.alinous.script.ISQLSentence;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElement;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.DeleteSentence;
import org.alinous.script.sql.FromClause;
import org.alinous.script.sql.GroupByClause;
import org.alinous.script.sql.InsertSentence;
import org.alinous.script.sql.LimitOffsetClause;
import org.alinous.script.sql.OrderByClause;
import org.alinous.script.sql.SelectColumns;
import org.alinous.script.sql.SelectSentence;
import org.alinous.script.sql.SetClause;
import org.alinous.script.sql.UpdateSentence;
import org.alinous.script.sql.WhereClause;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.ddl.AlterAdd;
import org.alinous.script.sql.ddl.AlterDrop;
import org.alinous.script.sql.ddl.CheckDefinition;
import org.alinous.script.sql.ddl.ColumnTypeDescriptor;
import org.alinous.script.sql.ddl.ForeignKey;
import org.alinous.script.sql.ddl.IAlterAction;
import org.alinous.script.sql.ddl.Unique;
import org.alinous.script.sql.lock.ForUpdateClause;
import org.alinous.script.sql.other.ColumnList;
import org.alinous.script.sql.other.SelectColumnElement;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.TablesList;
import org.alinous.script.sql.other.VariableList;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.sql.statement.SQLVariable;


public class PostgreSQLDataSource implements IAlinousDataSource
{
	public static boolean usePrecompileSelect = AlinousCore.usePrecompileSelect();
	public static boolean usePrecompileUpdate = AlinousCore.usePrecompileUpdate();
	public static boolean usePrecompileInsert = AlinousCore.usePrecompileInsert();
	public static boolean usePrecompileDelete = AlinousCore.usePrecompileDelete();
	
	private String user;
	private String pass;
	private String uri;
	private int maxclients;
	
	private PostgreSQLConnectionFactory factory;
	private AlinousConnectionPool connectionPool;
	
	private Driver driver;
	private ILogProvidor logger;
	private boolean resultUpper = false;
	
	//private Statement batchStmt;
	//private Map<Connection, Statement> batchedStatementMap = new HashMap<Connection, Statement>();
	
	private boolean outSql = true;
	
	private TypeHelper typeHelper;
	private AlinousCore core;
	
	public PostgreSQLDataSource()
	{
		
	}
	
	public void init(AlinousCore core) throws DataSourceException
	{
		try {
			driver = (Driver)Class.forName("org.postgresql.Driver").newInstance();
		} catch (ClassNotFoundException e) {
			throw new DataSourceException(e);
		} catch (InstantiationException e) {
			throw new DataSourceException(e);
		} catch (IllegalAccessException e) {
			throw new DataSourceException(e);
		}
		
		try {
			this.factory = new PostgreSQLConnectionFactory(core, this.driver, this.user, this.pass, this.uri, this.maxclients, this.resultUpper);
		} catch (Exception e) {
			throw new DataSourceException(e);
		}
		
		this.connectionPool = new AlinousConnectionPool(this.factory);
		
		this.typeHelper = new TypeHelper(this);
	}
	
	@Override
	public Object connect() throws DataSourceException
	{
		PostgreSqlConnection con;
		try {
			con = (PostgreSqlConnection) this.connectionPool.borrowObject();
		} catch (Exception e) {
			throw new DataSourceException(e);
		}

		return con;
	}
	
	@Override
	public void close(Object connectObj)
	{
		if(connectObj == null){
			return;
		}

		try {
			this.connectionPool.returnObject(connectObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void createDatabase(Object connectionHandle, String dbName)
		throws DataSourceException
	{
		StringBuffer sqlString = new StringBuffer();
		sqlString.append("CREATE DATABASE ");
		sqlString.append(dbName);
		
		executeUpdateSQL(connectionHandle, sqlString.toString(), false);
	}
	
	@Override
	public void dropDatabase(Object connectionHandle, String dbName)
		throws DataSourceException
	{
		StringBuffer sqlString = new StringBuffer();
		sqlString.append("DROP DATABASE ");
		sqlString.append(dbName);
		
		executeUpdateSQL(connectionHandle, sqlString.toString(), false);
	}
	
	@Override
	public void createTable(Object connectionHandle, DataTable table) throws DataSourceException
	{
		//TypeHelper helper = this.typeHelper.newHelper(false, sentence);
		List<DataField> fields = new LinkedList<DataField>();
		List<String> indexes = new LinkedList<String>();
		
		List<String> keys = table.getFields();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String fld = it.next();
			DataField df = table.getDataField(fld);
			
			fields.add(df);
			
		//	if(df.isPrimary()){
		//		indexes.add(fld);
		//	}
		}
		
		Iterator<DataField> primaryIt = table.getPrimaryKeys().iterator();
		while(primaryIt.hasNext()){
			DataField pf = primaryIt.next();
			
			indexes.add(pf.getName());
		}
		
		
		// Make SQL String
		StringBuffer sqlString = new StringBuffer();
		sqlString.append("CREATE TABLE ");
		sqlString.append(table.getName());
		sqlString.append(" ( \n");
		
		boolean first = true;
		Iterator<DataField> itDf = fields.iterator();
		while(itDf.hasNext()){
			DataField fld = itDf.next();
			
			if(first){
				first = false;
			}else{
				sqlString.append(",\n");
			}
			
			sqlString.append(fld.getName());
			sqlString.append(" ");
			sqlString.append(getSQLType(fld.getType()));
			
			// not null
			if(fld.isNotnull()){
				sqlString.append(" NOT NULL");
			}
			
			// unique
			if(fld.isUnique()){
				sqlString.append(" UNIQUE");
			}
			
			// default
			if(fld.getDefaultValue() != null){
				sqlString.append(" ");
				sqlString.append(fld.getDefaultString());
			}
			
			// check
			if(fld.getCheck() != null){
				sqlString.append(" ");
				try {
					sqlString.append(fld.getCheck().extract(null, null, null, null, null));
				} catch (ExecutionException e) {
					e.printStackTrace();
					throw new DataSourceException(e);
				}
			}
		}
		
		// check
		Iterator<CheckDefinition> checkIt = table.getCheck().iterator();
		while(checkIt.hasNext()){
			CheckDefinition def = checkIt.next();
			
			sqlString.append(",\n");
			try {
				sqlString.append(def.extract(null, null, null, null, null));
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new DataSourceException(e);
			}
		}
		
		// foreign key
		Iterator<ForeignKey> foreignIt = table.getForeignKey().iterator();
		while(foreignIt.hasNext()){
			ForeignKey fkey = foreignIt.next();
			
			sqlString.append(",\n");
			try {
				sqlString.append(fkey.extract(null, null, null, null, null));
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new DataSourceException(e);
			}
		}
		
		// primary key
		if(indexes.size() > 0){
			sqlString.append(",\n");
			sqlString.append("PRIMARY KEY(");
			
			first = true;
			it = indexes.iterator();
			while(it.hasNext()){
				String idxFld = it.next();
				
				if(first){
					first = false;
				}else{
					sqlString.append(", ");
				}
				
				sqlString.append(idxFld);
			}

			sqlString.append(")");
		}
		
		// unique
		Iterator<Unique> itU = table.getUnique().iterator();
		while(itU.hasNext()){
			Unique unique = itU.next();
			
			sqlString.append(",\n");
			sqlString.append("UNIQUE(");
			
			first = true;
			Iterator<String> itUnique = unique.getKey().iterator();
			while(itUnique.hasNext()){
				String col = itUnique.next();
				
				if(first){
					first = false;
				}else{
					sqlString.append(",");
				}
				
				sqlString.append(col);
			}
			
			sqlString.append(")");
		}
		
		sqlString.append("\n)\n");
		
		if(this.logger != null && this.outSql){
			this.logger.reportInfo(sqlString.toString());
		}
		
		// 
		AlinousDebug.debugOut(core, sqlString.toString());
		
		executeUpdateSQL(connectionHandle, sqlString.toString(), false);

	}
	
	public void executeUpdateSQLPrecompile(Object connectionHandle, String sql, boolean trxStarted
			,PostContext context, VariableRepository provider)
		throws DataSourceException, ExecutionException
	{
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(sql, context);
			
			doPrecompiledUpdateExecute(context, provider, connectionHandle, pstmt, context.getPrecompile().getPrecompileValues(), trxStarted, false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DataSourceException(e);
		}
		
	}
	
	private void doPrecompiledUpdateExecute(PostContext context,
					VariableRepository provider, Object connectionHandle,
						PreparedStatement pstmt, List<PreCompileValue> valList, boolean trxStarted, boolean cached) throws DataSourceException, ExecutionException
	{
		try {
			setupPrecompileParamns(context, provider, valList, pstmt);
			
			// force show precompile indicator
			//this.outSql = true; 
			
			if(this.outSql){
				String prefix = "[P] ";
				if(cached){
					prefix = "[CP] ";
				}
				AlinousDebug.debugOut(core, AlinousDebug.trimLength(prefix + pstmt, 2048));
			}
			
			pstmt.executeUpdate();
			

		} catch (SQLException e) {
			AlinousDebug.debugOut(core, "[ERROR]" + pstmt);
			e.printStackTrace();
			throw new DataSourceException(e);
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw e;
		} finally{
			try {
				if(!pstmt.isPoolable()){
					pstmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DataSourceException(e);
			}
		}

	}

	public void executeUpdateSQL(Object connectionHandle, String sql, boolean trxStarted)
			throws DataSourceException
	{
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		
		/*
		if(trxStarted){
			try {
				executeAddBatch(con, sql);
			} catch (SQLException e) {
				throw new DataSourceException(e);
			}
			return;
		}
		*/
		Statement stmt = null;
		try {
				stmt = con.createStatement();
				stmt.executeUpdate(sql);
		} catch (SQLException e) {
			AlinousDebug.debugOut(core, "failed sql : " + sql);
			throw new DataSourceException(e);
		}finally{
			if(stmt != null){
				try {
					stmt.close();
				} catch (SQLException e) {	}
			}
		}
	}
	/*
	private void executeAddBatch(Connection con, String sql) throws SQLException
	{
		Statement batchStmt = this.batchedStatementMap.get(con);
		if(batchStmt == null){
			batchStmt = con.createStatement();
			this.batchedStatementMap.put(con, batchStmt);
		}
		
		batchStmt.addBatch(sql);
	}*/
	
	
	private String getSQLType(String type)
	{
		if(type.equals(DataField.TYPE_STRING)){
			return "VARCHAR(256)";
		}
		if(type.equals(DataField.TYPE_TEXT_STRING)){
			return "TEXT";
		}
		if(type.equals(DataField.TYPE_INTEGER)){
			return "INTEGER";
		}
		if(type.equals(DataField.TYPE_DOUBLE)){
			return "REAL";
		}
		if(type.equals(DataField.TYPE_TIMESTAMP)){
			return "TIMESTAMP";
		}
		if (type.equals(DataField.TYPE_DATE)) {
			return "DATE";
		}
		
		return "VARCHAR(256)";
	}
	
	@Override
	public void dropTable(Object connectionHandle, String table) throws DataSourceException
	{
		// Make SQL String
		StringBuffer sqlString = new StringBuffer();
		sqlString.append("DROP TABLE ");
		sqlString.append(table);

		executeUpdateSQL(connectionHandle, sqlString.toString(), false);
	}

	@Override
	public void insert(Object connectionHandle, Record record, String table, boolean trxStarted, PostContext context, VariableRepository provider)
					throws DataSourceException
	{
		String sql = makeInsertString(record, table);
		
		// out string
		if(this.outSql){
			AlinousDebug.println(sql);
		}
		
		executeUpdateSQL(connectionHandle, sql, trxStarted);
	}
	
	private String makeInsertString(Record record, String table)
	{
		StringBuffer sql = new StringBuffer();
		
		sql.append("INSERT INTO ");
		sql.append(table);
	
		sql.append(" (");
		boolean first = true;
		Iterator<String> itFld = record.getMap().keySet().iterator();
		while(itFld.hasNext()){
			String field = itFld.next();
			
			if(first){
				first = false;
			}else{
				sql.append(", ");
			}
			
			sql.append(field);
		}
		sql.append(")");
		
		sql.append(" VALUES ");
		
		outRecordValue(record, sql);
		
		sql.append(";");
		
		return sql.toString();
	}
	
	public void insertPrecompile(Object connectionHandle, List<Record> records, String table, boolean trxStarted, PostContext context, VariableRepository provider)
		throws DataSourceException, ExecutionException
	{
		ColumnList cols = new ColumnList();
		
		// Cols
		Iterator<String> itCol = records.get(0).getMap().keySet().iterator();
		while(itCol.hasNext()){
			String colName = itCol.next();
			
			SelectColumnElement col = new SelectColumnElement();
			col.setColumnName(colName);
			
			cols.addColumns(col);
		}
		
		VariableRepository valRepo = new VariableRepository();
		Iterator<Record> it = records.iterator();
		while(it.hasNext()){
			Record rec = it.next();
			
			VariableList list = new VariableList();
			
			Iterator<String> keyIt = rec.getMap().keySet().iterator();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				String value = rec.getFieldValue(key);
				try {
					valRepo.putValue(key, value, rec.getFieldType(key), context);
				} catch (RedirectRequestException e) {
					e.printStackTrace();
					context.getCore().getLogger().reportError(e);
				}
				
				SQLVariable sqlVal = new SQLVariable();
				sqlVal.setPrefix("$");
				sqlVal.setPathElement(new PathElement(key));
				
				list.addValues(sqlVal);
			}
			
			ArrayList<VariableList> tmpList = new ArrayList<VariableList>();
			tmpList.add(list);
			
			insert(connectionHandle, cols, tmpList, table, trxStarted, context, valRepo);
		}
	}
	
	@Override
	public void insert(Object connectionHandle, List<Record> records, String table, boolean trxStarted, PostContext context, VariableRepository provider)
									throws DataSourceException
	{
		
		if(usePrecompileUpdate && context.getPrecompile().getSqlSentence() != null 
				&& context.getPrecompile().getSqlSentence().isPrecompilable()){
			try {
				insertPrecompile(connectionHandle, records, table, trxStarted, context, provider);
			} catch (ExecutionException e) {
				e.printStackTrace();
				new DataSourceException(e);
			}
			return;
		}
		
		context.getPrecompile().setCompile(false);
		
		/*
		StringBuffer buff = new StringBuffer();
		
		Iterator<Record> it = records.iterator();
		while(it.hasNext()){
			Record rec = it.next();
			
			String sql = makeInsertString(rec, table);
			
			buff.append(sql);
			buff.append("\n");
			//insert(connectionHandle, rec, table);
		}
		
		// out string
		if(this.outSql){
			AlinousDebug.println(buff.toString());
		}
		executeUpdateSQL(connectionHandle, buff.toString(), trxStarted);
		*/
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("INSERT INTO ");
		sql.append(table);
		
		sql.append(" (");
		boolean first = true;
		Iterator<String> itFld = records.get(0).getMap().keySet().iterator();
		while(itFld.hasNext()){
			String field = itFld.next();
			
			if(first){
				first = false;
			}else{
				sql.append(", ");
			}
			
			sql.append(field);
		}
		sql.append(")");
		
		sql.append(" VALUES ");
		
		
		first = true;
		Iterator<Record> it = records.iterator();
		while(it.hasNext()){
			Record rec = it.next();
			StringBuffer eachSql = new StringBuffer(sql);
			
			outRecordValue(rec, eachSql);
			
			executeUpdateSQL(connectionHandle, eachSql.toString(), trxStarted);
		}
		
		// out string
		//AlinousDebug.debugOut(sql.toString());
		//executeUpdateSQL(connectionHandle, sql.toString());*/
	}
	
	public void insertPrecompile(Object connectionHandle, ColumnList cols, 
			ArrayList<VariableList> valueLists, String table, boolean trxStarted
			, PostContext context, VariableRepository provider)
							throws DataSourceException, ExecutionException
	{
		context.getPrecompile().clear();
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;

		// second
		PreparedStatement pstmt = con.getPreparedStatement(context.getPrecompile().getSqlSentence());
		List<PreCompileValue> valListPre = con.getListPreCompileValue(context.getPrecompile().getSqlSentence());
		
		if(pstmt != null && valListPre != null){
			// 
			//AlinousDebug.debugOut("insert cached... (last exec)-> " + pstmt);
			
			doPrecompiledUpdateExecute(context, provider, connectionHandle, pstmt, valListPre, trxStarted, true);
			return;
		}
		
		// first
		InsertSentence sentence = new InsertSentence();
		sentence.setCols(cols);
		TableIdentifier tbl = new TableIdentifier(table);
		sentence.setTbl(tbl);
		
		TypeHelper helper = this.typeHelper.newHelper(false, sentence);
		helper.setDisableCounter(false);
				
		StringBuffer sql = new StringBuffer();
		
		sql.append("INSERT INTO ");
		sql.append(table);
		
		context.getPrecompile().setCompile(true);
		
		// col definition
		if(cols != null && cols.isReady(context, provider, null)){
			sql.append(" ( ");
			
			boolean first = true;
			Iterator<SelectColumnElement> it = cols.iterator();
			while(it.hasNext()){
				SelectColumnElement element = it.next();
				
				if(first){
					first = false;
				}
				else{
					sql.append(", ");
				}

				
				try {
					sql.append(element.extract(context, provider, null, null, helper));
				} catch (RedirectRequestException e) {
					e.printStackTrace();
				}
				String columnName = element.getColumnName().extract(context, provider, null, null, helper);
				
				String fldType = helper.getDataFieldType(table + "." + columnName, context);
				helper.addListType(fldType);
				
			}
			sql.append(" ) ");
		}
		else{
			// setup cols
			DataTable dt = getDataTable(connectionHandle, table);
			
			Iterator<DataField> it = dt.iterator();
			while(it.hasNext()){
				DataField fld = it.next();
				helper.addListType(fld.getType());
			}
		}
		
		// values
		sql.append(" VALUES ");
		
		helper.resetCount();
		boolean first = true;
		Iterator<VariableList> valListIt = valueLists.iterator();
		while(valListIt.hasNext()){
			VariableList valList = valListIt.next();
			
			if(first){
				first = false;
			}
			else{
				sql.append(", ");
			}
			
			sql.append(valList.extract(context, provider, null, null, helper));
		}
		
		executeUpdateSQLPrecompile(connectionHandle, sql.toString(), trxStarted, context, provider);
		
		context.getPrecompile().setCompile(false);
	}
	
	/**
	 * insert sentence use this function
	 */
	@Override
	public void insert(Object connectionHandle, ColumnList cols, 
			ArrayList<VariableList> valueLists, String table, boolean trxStarted
			, PostContext context, VariableRepository provider)
							throws DataSourceException, ExecutionException
	{
		if(usePrecompileUpdate && context != null && context.getPrecompile().getSqlSentence() != null
				&& valueLists.size() == 1
				&& context.getPrecompile().getSqlSentence().isPrecompilable()){
			insertPrecompile(connectionHandle, cols, valueLists, table, trxStarted, context, provider);
			return;
		}
		
		if(context != null){
			context.getPrecompile().setCompile(false);
		}
		
		InsertSentence sentence = new InsertSentence();
		sentence.setCols(cols);
		TableIdentifier tbl = new TableIdentifier(table);
		sentence.setTbl(tbl);
		
		TypeHelper helper = this.typeHelper.newHelper(false, sentence);
				
		StringBuffer sql = new StringBuffer();
		
		sql.append("INSERT INTO ");
		sql.append(table);
		
		// col definition
		if(cols != null && cols.isReady(context, provider, null)){
			sql.append(" ( ");
			
			boolean first = true;
			Iterator<SelectColumnElement> it = cols.iterator();
			while(it.hasNext()){
				SelectColumnElement element = it.next();
				
				if(first){
					first = false;
				}
				else{
					sql.append(", ");
				}
				
				
				try {
					sql.append(element.extract(context, provider, null, null, helper));
				} catch (RedirectRequestException e) {
					e.printStackTrace();
				}
				String columnName = element.getColumnName().extract(context, provider, null, null, helper);
				
				String fldType = helper.getDataFieldType(table + "." + columnName, context);
				
				// debug
				// AlinousDebug.debugOut("********* Type helper : " + columnName + " -> " + fldType);
				
				helper.addListType(fldType);
			}
			sql.append(" ) ");
		}
		else{
			// setup cols
			DataTable dt = getDataTable(connectionHandle, table);
			
			Iterator<DataField> it = dt.iterator();
			while(it.hasNext()){
				DataField fld = it.next();
				helper.addListType(fld.getType());
			}
		}
		
		// values
		sql.append(" VALUES ");
		
		helper.resetCount();
		boolean first = true;
		Iterator<VariableList> valListIt = valueLists.iterator();
		while(valListIt.hasNext()){
			VariableList valList = valListIt.next();
			
			if(first){
				first = false;
			}
			else{
				sql.append(", ");
			}
			
			//helper.resetCount();
			
			sql.append(valList.extract(context, provider, null, null, helper));
		}
		
		if(this.outSql){
			AlinousDebug.debugOut(core, sql.toString());
		}
		
		executeUpdateSQL(connectionHandle, sql.toString(), trxStarted);
	}
	
	private void outRecordValue(Record record, StringBuffer sql)
	{
		sql.append("(");
		
		boolean first = true;
		Map<String, String> map = record.getMap();
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()){
			if(first){
				first = false;
			}else{
				sql.append(", ");
			}
			
			String val = map.get(it.next());
			
			if(val != null){
				sql.append("'");
				sql.append(AlinousUtils.sqlEscape(val));
				sql.append("'");
			}else{
				sql.append("NULL");
			}
		}
		
		sql.append(")");
	}

	public void deletePrecompile(Object connectionHandle, String fromStr, WhereClause where
			, boolean trxStarted, PostContext context, VariableRepository provider, AdjustWhere adjWhere)
										throws DataSourceException, ExecutionException
	{
		context.getPrecompile().clear();
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		
		// second
		PreparedStatement pstmt = con.getPreparedStatement(context.getPrecompile().getSqlSentence());
		List<PreCompileValue> valList = con.getListPreCompileValue(context.getPrecompile().getSqlSentence());
		
		if(pstmt != null && valList != null){
			//  
			//AlinousDebug.debugOut("Delete cached... (last exec)-> " + pstmt);
			
			doPrecompiledUpdateExecute(context, provider, connectionHandle, pstmt, valList, trxStarted, true);
			return;
		}
		
		DeleteSentence sentence = new DeleteSentence();
		String[] fromTokens = fromStr.split(" ");
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		tableList.addTable(new TableIdentifier(fromTokens[1]));
		from.setTableList(tableList);
		sentence.setFrom(from);
		
		TypeHelper helper = this.typeHelper.newHelper(false, sentence);
	
		StringBuffer sql = new StringBuffer();
		
		sql.append("DELETE ");
		sql.append(fromStr);
		
		context.getPrecompile().setCompile(true);
		if(where != null && where.isReady(context, provider, adjWhere)){
			String wh = where.extract(context, provider, adjWhere, null, helper);
			
			if(context.getPrecompile().isHasArray()){
				context.getPrecompile().setCompile(false);
				
				if(context.getPrecompile().getSqlSentence() != null){
					context.getPrecompile().getSqlSentence().setPrecompilable(false);
				}
				
				//delete(connectionHandle, fromStr, where, trxStarted, context, provider, adjWhere);
				return;
			}
			
			sql.append(" ");
			sql.append(wh);
		}
		
		executeUpdateSQLPrecompile(connectionHandle, sql.toString(), trxStarted, context, provider);
		
		context.getPrecompile().setCompile(false);
	}
	
	@Override
	public void delete(Object connectionHandle, String fromStr, WhereClause where
					, boolean trxStarted, PostContext context, VariableRepository provider, AdjustWhere adjWhere)
												throws DataSourceException, ExecutionException
	{
		if(usePrecompileDelete && context != null
				&& context.getPrecompile().getSqlSentence() != null
				&& (adjWhere == null || !adjWhere.adjust())
				/*&& !context.getPrecompile().isHasArray()*/
				&& context.getPrecompile().getSqlSentence().isPrecompilable()){
			deletePrecompile(connectionHandle, fromStr, where, trxStarted, context, provider, adjWhere);
			return;
		}
		if(context != null){
			context.getPrecompile().setCompile(false);
		}
		
		DeleteSentence sentence = new DeleteSentence();
		String[] fromTokens = fromStr.split(" ");
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		tableList.addTable(new TableIdentifier(fromTokens[1]));
		from.setTableList(tableList);
		sentence.setFrom(from);
		
		TypeHelper helper = this.typeHelper.newHelper(false, sentence);
	
		StringBuffer sql = new StringBuffer();
		
		sql.append("DELETE ");
		sql.append(fromStr);
		
		if(where != null && where.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(where.extract(context, provider, adjWhere, null, helper));
		}
		
		// out string
		if(this.outSql){
			AlinousDebug.debugOut(core, sql.toString());
		}
		executeUpdateSQL(connectionHandle, sql.toString(), trxStarted);
	}
	
	public void updatePrecompile(Object connectionHandle, String table, SetClause set, WhereClause where
			, boolean trxStarted, PostContext context, VariableRepository provider, AdjustWhere adjWhere, AdjustSet adjSet)
	throws DataSourceException, ExecutionException
	{
		context.getPrecompile().clear();
		context.getPrecompile().clearStatus();
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		
		// second
		PreparedStatement pstmt = con.getPreparedStatement(context.getPrecompile().getSqlSentence());
		List<PreCompileValue> valList = con.getListPreCompileValue(context.getPrecompile().getSqlSentence());
		
		if(pstmt != null && valList != null){
			// 
			//AlinousDebug.debugOut("update cached... (last exec)-> " + pstmt);
			
			doPrecompiledUpdateExecute(context, provider, connectionHandle, pstmt, valList, trxStarted, true);
			return;
		}
		
		UpdateSentence sentence = new UpdateSentence();
		sentence.setTable(new TableIdentifier(table));
		TypeHelper helper = this.typeHelper.newHelper(false, sentence);
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("UPDATE ");
		sql.append(table);
		
		context.getPrecompile().setCompile(true);
		if(set != null && set.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(set.extractPreCompile(context, provider, adjWhere, adjSet, helper));
			
			if(context.getPrecompile().isHasArray()){
				context.getPrecompile().setCompile(false);
				
				if(context.getPrecompile().getSqlSentence() != null){
					context.getPrecompile().getSqlSentence().setPrecompilable(false);
				}
				
				update(connectionHandle, table, set, where, trxStarted, context, provider, adjWhere, adjSet);
				return;
			}
		}
		
		if(where != null && where.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(where.extract(context, provider, adjWhere, adjSet, helper));
			
			if(context.getPrecompile().isHasArray()){
				context.getPrecompile().setCompile(false);
				
				if(context.getPrecompile().getSqlSentence() != null){
					context.getPrecompile().getSqlSentence().setPrecompilable(false);
				}
				
				update(connectionHandle, table, set, where, trxStarted, context, provider, adjWhere, adjSet);
				return;
			}
		}
		
		executeUpdateSQLPrecompile(connectionHandle, sql.toString(), trxStarted, context, provider);
		
		context.getPrecompile().setCompile(false);
	}
	
	@Override
	public void update(Object connectionHandle, String table, SetClause set, WhereClause where
						, boolean trxStarted, PostContext context, VariableRepository provider, AdjustWhere adjWhere, AdjustSet adjSet)
												throws DataSourceException, ExecutionException
	{
		if(usePrecompileUpdate && context != null && context.getPrecompile().getSqlSentence() != null 
				&& (adjWhere == null || !adjWhere.adjust())
				/*&& !context.getPrecompile().isHasArray()*/
				&& context.getPrecompile().getSqlSentence().isPrecompilable()){
			updatePrecompile(connectionHandle, table, set, where, trxStarted, context, provider, adjWhere, adjSet);
			return;
		}
		
		if(context != null){
			context.getPrecompile().setCompile(false);
		}
		
		UpdateSentence sentence = new UpdateSentence();
		sentence.setTable(new TableIdentifier(table));
		TypeHelper helper = this.typeHelper.newHelper(false, sentence);
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("UPDATE ");
		sql.append(table);
		
		if(set != null && set.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(set.extract(context, provider, adjWhere, adjSet, helper));
		}
		
		if(where != null && where.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(where.extract(context, provider, adjWhere, adjSet, helper));
		}
		
		// out string
		if(this.outSql){
			AlinousDebug.debugOut(core, sql.toString());
		}
		executeUpdateSQL(connectionHandle, sql.toString(), trxStarted);
	}
	
	@Override
	public List<Record> select(Object connectionHandle, String distinct, SelectColumns columns, FromClause from, WhereClause where,
			GroupByClause groupBy, OrderByClause orderBy, LimitOffsetClause limit, ForUpdateClause forupdate
			, PostContext context, VariableRepository provider, AdjustWhere adjWhere)
			throws DataSourceException, ExecutionException
	{
		if(usePrecompileSelect && context != null && context.getPrecompile().getSqlSentence() != null 
				&& (adjWhere == null || !adjWhere.adjust())
				/*&& !context.getPrecompile().isHasArray()*/
				&& context.getPrecompile().getSqlSentence().isPrecompilable()){
			return selectPrecompile(connectionHandle, distinct, columns, from, where, groupBy, orderBy, limit, forupdate, context, provider, adjWhere);
		}
		
		/*
		AlinousDebug.debugOut("@@@ context : " + context);
		AlinousDebug.debugOut("@@@ adjWhere : " + adjWhere);
		if(adjWhere != null){
			AlinousDebug.debugOut("@@@ adjWhere : " + adjWhere.adjust());
		}
		if(context != null){
			AlinousDebug.debugOut("@@@ context.getPrecompile().getSqlSentence() : " + context.getPrecompile().getSqlSentence());
			if(context.getPrecompile() != null && context.getPrecompile().getSqlSentence() != null){
				AlinousDebug.debugOut("@@@ context.getPrecompile().getSqlSentence().isPrecompilable() : " + context.getPrecompile().getSqlSentence().isPrecompilable());
			}
		}*/
				
		SelectSentence sentence = new SelectSentence();
		sentence.setFrom(from);
		TypeHelper helper = this.typeHelper.newHelper(false, sentence);
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("SELECT ");
		
		if(distinct != null){
			sql.append("DISTINCT ");
		}
		
		sql.append(columns.extract(context, provider, adjWhere, null, helper));
		sql.append(" ");
		
		if(from != null){
			sql.append(from.extract(context, provider, adjWhere, null, helper));
		}
		
		if(where != null && where.isReady(context, provider, adjWhere)){
			sql.append(" ");
			
			sql.append(where.extract(context, provider, adjWhere, null, helper));
		}
		if(groupBy != null && groupBy.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(groupBy.extract(context, provider, adjWhere, null, helper));
		}
		if(orderBy != null && orderBy.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(orderBy.extract(context, provider, adjWhere, null, helper));
		}
		if(limit != null && limit.isReady(context, provider, adjWhere)){
			String lim = limit.extract(context, provider, adjWhere, null, helper);
			
			sql.append(" ");
			sql.append(lim);
		}
		if(forupdate != null && forupdate.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(forupdate.extract(context, provider, adjWhere, null, helper));
		}
		
		// out String
		if(this.outSql){
			AlinousDebug.debugOut(core, sql.toString());
		}
		//  debug
		//AlinousDebug.debugOut(sql.toString());
		
		List<Record> tmp = executeSelectSQL(connectionHandle, sql.toString());
		return tmp;
	}
	
	
	public List<Record> selectPrecompile(Object connectionHandle, String distinct, SelectColumns columns, FromClause from, WhereClause where,
			GroupByClause groupBy, OrderByClause orderBy, LimitOffsetClause limit, ForUpdateClause forupdate
			, PostContext context, VariableRepository provider, AdjustWhere adjWhere)
			throws DataSourceException, ExecutionException
	{
		context.getPrecompile().clear();
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		
		// second
		PreparedStatement pstmt = con.getPreparedStatement(context.getPrecompile().getSqlSentence());
		List<PreCompileValue> valList = con.getListPreCompileValue(context.getPrecompile().getSqlSentence());
		
		if(pstmt != null && valList != null){
			// 
			//AlinousDebug.debugOut("select cached... (last exec)-> " + pstmt);
			
			return doPrecompiledSelectExecute(context, provider, connectionHandle, pstmt, valList, true);
		}
		
		// first
		SelectSentence sentence = new SelectSentence();
		sentence.setFrom(from);
		TypeHelper helper = this.typeHelper.newHelper(false, sentence);
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("SELECT ");
		
		if(distinct != null){
			sql.append("DISTINCT ");
		}
		
		context.getPrecompile().setCompile(true);
		
		sql.append(columns.extract(context, provider, adjWhere, null, helper));
		sql.append(" ");
		
		context.getPrecompile().setMarkVariable(false);
		context.getPrecompile().setHasArray(false);
		if(from != null){
			String fromstr = from.extract(context, provider, adjWhere, null, helper);
			
			if(context.getPrecompile().isHasArray() || context.getPrecompile().isMarkVariable()){
				context.getPrecompile().setCompile(false);
				
				if(context.getPrecompile().getSqlSentence() != null){
					context.getPrecompile().getSqlSentence().setPrecompilable(false);
				}
				
				return select(connectionHandle, distinct, columns, from, where, groupBy, orderBy, limit, forupdate, context, provider, adjWhere);
			}
			
			sql.append(fromstr);
		}
		
		// precompile
		context.getPrecompile().setCompile(true);
		context.getPrecompile().setHasArray(false);
		if(where != null && where.isReady(context, provider, adjWhere)){
			sql.append(" ");
			
			String wh = where.extract(context, provider, adjWhere, null, helper);
			
			if(context.getPrecompile().isHasArray()){
				context.getPrecompile().setCompile(false);
				
				if(context.getPrecompile().getSqlSentence() != null){
					context.getPrecompile().getSqlSentence().setPrecompilable(false);
				}
				
				return select(connectionHandle, distinct, columns, from, where, groupBy, orderBy, limit, forupdate, context, provider, adjWhere);
			}
			
			sql.append(wh);
		}
		
		if(groupBy != null && groupBy.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(groupBy.extract(context, provider, adjWhere, null, helper));
		}
		
		context.getPrecompile().setMarkVariable(false);
		context.getPrecompile().setHasArray(false);
		if(orderBy != null && orderBy.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(orderBy.extract(context, provider, adjWhere, null, helper));
			
			if(context.getPrecompile().isHasArray() /*|| context.getPrecompile().isMarkVariable()*/){
				context.getPrecompile().setCompile(false);
				
				if(context.getPrecompile().getSqlSentence() != null){
					context.getPrecompile().getSqlSentence().setPrecompilable(false);
				}
				
				return select(connectionHandle, distinct, columns, from, where, groupBy, orderBy, limit, forupdate, context, provider, adjWhere);
			}
		}
		
		context.getPrecompile().setMarkVariable(false);
		if(limit != null && limit.isReady(context, provider, adjWhere)){
			String li = limit.extract(context, provider, adjWhere, null, helper);
			
			if(context.getPrecompile().isMarkVariable()){
				context.getPrecompile().setCompile(false);
				
				if(context.getPrecompile().getSqlSentence() != null){
					context.getPrecompile().getSqlSentence().setPrecompilable(false);
				}
				
				return select(connectionHandle, distinct, columns, from, where, groupBy, orderBy, limit, forupdate, context, provider, adjWhere);
			}
			
			sql.append(" ");
			sql.append(li);
		}
		
		context.getPrecompile().setCompile(false);
		if(forupdate != null && forupdate.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(forupdate.extract(context, provider, adjWhere, null, helper));
		}
		
		//debug
		//AlinousDebug.debugOut("SQL : " + sql.toString());
		
		
		List<Record> tmp = executeSelectSQLPrecompile(context, provider, connectionHandle, sql.toString());
		context.getPrecompile().setSqlSentence(null);
		
		return tmp;
	}
	
	private List<Record> executeSelectSQLPrecompile(PostContext context, VariableRepository provider, Object connectionHandle, String sql) throws DataSourceException, ExecutionException
	{
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		PreparedStatement pstmt = null;
		
		List<Record> retList = null;
		try {
			pstmt = con.prepareStatement(sql, context);

			retList = doPrecompiledSelectExecute(context, provider, connectionHandle, pstmt, context.getPrecompile().getPrecompileValues(), false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DataSourceException(e);
		}finally{
			try {
				if(!pstmt.isPoolable()){
					pstmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DataSourceException(e);
			}
		}
		
		return retList;
	}
	
	private List<Record> doPrecompiledSelectExecute(PostContext context, VariableRepository provider, Object connectionHandle,
			PreparedStatement pstmt, List<PreCompileValue> values, boolean cached) throws ExecutionException, DataSourceException{

		
		List<Record> retList = new LinkedList<Record>();
		try {
			setupPrecompileParamns(context, provider, values, pstmt);
			
			ResultSet rs = pstmt.executeQuery();
			
			if(this.outSql){
				String prefix = "[P] ";
				if(cached){
					prefix = "[CP] ";
				}
				AlinousDebug.debugOut(core, prefix + pstmt);
			}
			
			ResultSetMetaData metaData = rs.getMetaData();
			while(rs.next()){
				
				
				int cnt = metaData.getColumnCount();
				Record rec = new Record();
				for(int i = 0; i < cnt; i++){
					// metadata low or upper
					
					String colName = metaData.getColumnName(i + 1);
					if(this.resultUpper){
						colName = colName.toUpperCase();
					}
					
					String value = rs.getString(i + 1);
					
					int colType = metaData.getColumnType(i + 1);
					rec.addFieldValue(colName, value, colType);
				}
				
				retList.add(rec);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DataSourceException(e);
		}finally{
			
		}

		return retList;
	}
	
	private List<Record> executeSelectSQL(Object connectionHandle, String sql) throws DataSourceException
	{
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		Statement stmt = null;
		List<Record> retList = new LinkedList<Record>();
		
		try {
			stmt = con.createStatement();
			
			stmt.execute(sql);
			
			ResultSet rs = stmt.getResultSet();
			ResultSetMetaData metaData = rs.getMetaData();
			while(rs.next()){
				int cnt = metaData.getColumnCount();
				Record rec = new Record();
				for(int i = 0; i < cnt; i++){
					// metadata low or upper
					
					String colName = metaData.getColumnName(i + 1);
					if(this.resultUpper){
						colName = colName.toUpperCase();
					}
					String value = rs.getString(i + 1);
					
					int colType = metaData.getColumnType(i + 1);
					rec.addFieldValue(colName, value, colType);
				}
				
				retList.add(rec);
			}

		} catch (Throwable e) {
			e.printStackTrace();
			throw new DataSourceException(e);
		}finally{
			try {
				stmt.close();
			} catch (SQLException ignore) {}
		}
		
		return retList;
	}
	
	private void setupPrecompileParamns(PostContext context, VariableRepository provider, List<PreCompileValue> values, PreparedStatement pstmt) throws ExecutionException, SQLException
	{
		// setup params
		int i = 1;
		Iterator<PreCompileValue> it = values.iterator();
		while(it.hasNext()){
			PreCompileValue v = it.next();
			
			IScriptVariable val = null;
			try {
				val = provider.getVariable(v.getPath(), context);
			} catch (RedirectRequestException e) {
				e.printStackTrace();
				context.getCore().getLogger().reportError(e);
			}
			
			if(val instanceof ScriptArray){
				/*String[] values = v.getArrayValue().toArray(new String[v.getArrayValue().size()]);
	            Array ar = new 
				//ArrayDescriptor ad = new ArrayDescriptor("TEST_ARRAY", con);

				//Array array = con.createArrayOf(dataFieldType2Pgsql(v.getType()), v.getArrayValue().toArray(new String[v.getArrayValue().size()]));
				
	           pstmt.setArray(i++, array);*/
				continue;
			}
			
			String value = null;
			if(val != null){
				value = ((ScriptDomVariable)val).getValue();
			}
			
			if(val == null || value == null){
				// Normal value
				if(v.getType().equals(DataField.TYPE_INTEGER)){
					pstmt.setNull(i++, java.sql.Types.INTEGER);
				}
				else if(v.getType().equals(DataField.TYPE_DOUBLE)){
					pstmt.setNull(i++, java.sql.Types.DOUBLE);
				}
				else if(v.getType().equals(DataField.TYPE_BOOLEAN)){
					pstmt.setNull(i++, java.sql.Types.BOOLEAN);
				}
				else if(v.getType().equals(DataField.TYPE_TIMESTAMP)){
					pstmt.setNull(i++, java.sql.Types.TIMESTAMP);
				}
				else if(v.getType().equals(DataField.TYPE_TIME)){
					pstmt.setNull(i++, java.sql.Types.TIME);
				}
				else if(v.getType().equals(DataField.TYPE_DATE)){
					pstmt.setNull(i++, java.sql.Types.DATE);
				}
				else{
					pstmt.setNull(i++, java.sql.Types.VARCHAR);
				}
				continue;
			}
			
			if(v.getType().equals(DataField.TYPE_INTEGER)){
				pstmt.setInt(i++, Integer.parseInt(value));
			}
			else if(v.getType().equals(DataField.TYPE_DOUBLE)){
				pstmt.setDouble(i++, Double.parseDouble(value));
			}
			else if(v.getType().equals(DataField.TYPE_BOOLEAN)){
				pstmt.setBoolean(i++, Boolean.parseBoolean(value));
			}
			else if(v.getType().equals(DataField.TYPE_TIMESTAMP)){
				if(value.length() > 23){
					value = value.substring(0, 24);
				}
				
				pstmt.setTimestamp(i++, Timestamp.valueOf(value));					
			}
			else if(v.getType().equals(DataField.TYPE_TIME)){
				pstmt.setTime(i++, Time.valueOf(value));					
			}
			else if(v.getType().equals(DataField.TYPE_DATE)){
				pstmt.setDate(i++, Date.valueOf(value));
			}
			else{
				pstmt.setString(i++, value);
			}
		}

	}
	
	// Getter and Setter
	public void setPass(String pass) {
		this.pass = pass;	
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public String getUri() {
		return uri;
	}

	public String getUser() {
		return user;
	}

	public int getMaxclients() {
		return maxclients;
	}

	public void setMaxclients(int maxclients) {
		this.maxclients = maxclients;
	}

	public DataTable getDataTable(Object connectionHandle, String tableName) throws DataSourceException
	{
		DataTable dataTable = null;
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		
		DatabaseMetaData metaData;
		ResultSet rsSet = null;
		try {
			metaData = con.getMetaData();
			rsSet = metaData.getTables(null, null, "%", new String[]{"TABLE"});
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}
		
		try {
			while(rsSet.next()){
				String tbl = rsSet.getString("TABLE_NAME");
				
				if(!tableName.toUpperCase().equals(tbl.toUpperCase())){
					continue;
				}
				// Create DataTable object
				dataTable = new DataTable();
				dataTable.setName(tbl);
				
				// Add column informations
				setupDataTableColumns(metaData, dataTable, tableName);
							
			}
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}finally{
			try {
				rsSet.close();
			} catch (SQLException ignore) {}
		}
		
		return dataTable;
	}
	
	private void setupDataTableColumns(DatabaseMetaData metaData, DataTable dataTable, String tableName)
					throws SQLException
	{
		ResultSet trs = metaData.getColumns("%", "%", tableName.toLowerCase(), "%");
		while(trs.next()){
			String columnName = trs.getString("COLUMN_NAME");
			String columnType = trs.getString("TYPE_NAME");
			
			DataField fld = new DataField();
			fld.setName(columnName.toUpperCase());
			
			
			// setType
			if(columnType.toUpperCase().equals("VARCHAR")){
				fld.setType(DataField.TYPE_STRING);
			}
			else if(columnType.toUpperCase().equals("TEXT")){
				fld.setType(DataField.TYPE_TEXT_STRING);
			}
			else if(columnType.toUpperCase().equals("INT2") || columnType.toUpperCase().equals("INT4") || columnType.toUpperCase().equals("INT8")){
				fld.setType(DataField.TYPE_INTEGER);
			}
			else if(columnType.toUpperCase().equals("TIMESTAMP")){
				fld.setType(DataField.TYPE_TIMESTAMP);
			}
			else if(columnType.toUpperCase().equals("TIME")){
				fld.setType(DataField.TYPE_TIME);
			}
			else if(columnType.toUpperCase().equals("DATE")){
				fld.setType(DataField.TYPE_DATE);
			}
			else if(columnType.toUpperCase().equals("FLOAT4") || columnType.toUpperCase().equals("FLOAT8")){
				fld.setType(DataField.TYPE_DOUBLE);
			}
			else{
				fld.setType(DataField.TYPE_STRING);
			}
			
			dataTable.addField(fld);
		}
		trs.close();

		// PrimaryKeys
		ResultSet primarysRs = metaData.getPrimaryKeys(null, null, tableName.toLowerCase());
		while(primarysRs.next()){
			String columnName = primarysRs.getString("COLUMN_NAME");
		
			DataField fld = dataTable.getDataField(columnName);
			fld.setPrimary(true);
			dataTable.addPrimaryKey(fld.getName());
		}
		primarysRs.close();
	}
	
	public String dataFieldType2Pgsql(String type)
	{
		if(type.equals(DataField.TYPE_BOOLEAN)){
			return "BOOL";
		}
		else if(type.equals(DataField.TYPE_TEXT_STRING)){
			return "Text";
		}
		else if(type.equals(DataField.TYPE_INTEGER)){
			return "int4";
		}
		else if(type.equals(DataField.TYPE_TIME)){
			return "Time";
		}
		else if(type.equals(DataField.TYPE_TIMESTAMP)){
			return "Timestamp";
		}
		else if(type.equals(DataField.TYPE_DATE)){
			return "Date";
		}
		else if(type.equals(DataField.TYPE_DOUBLE)){
			return "Double";
		}
		
		return "VARCHAR";
	}
	
	/*
	 * jdbc:postgresql://localhost:5432/alinous?xx-AAA
	 * 
	 */
	public String getDatabaseName()
	{
		String slashSplits[] = this.uri.split("/");
		String databaseSchem = slashSplits[slashSplits.length - 1];
		
		String dbName[] = databaseSchem.split("\\?");
		
		return dbName[0];
	}

	public void setLogger(ILogProvidor logger, AlinousCore core) {
		this.logger = logger;
		this.core = core;
	}

	public void setOutSql(boolean outSql)
	{
		this.outSql = outSql;
	}

	@Override
	public boolean begin(Object connectionHandle, int transactionIsolationLevel) throws DataSourceException
	{
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		boolean autoCommit;
		
		try {
			autoCommit = con.getAutoCommit();
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}
		
		try {
			con.setAutoCommit(false);
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}
		
		return autoCommit;
	}
	
	@Override
	public void commit(Object connectionHandle, boolean lastAutoCommit, String trxIdentifier) throws DataSourceException
	{
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		
		if(trxIdentifier != null){
			try {
				con.commitPrepared(trxIdentifier);
			}catch(Throwable e){
				throw new DataSourceException(e);
			}
			return;
		}
		
		try {
			con.commit();
			
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}finally{
			try {
				con.setAutoCommit(lastAutoCommit);
			} catch (SQLException e) {
				throw new DataSourceException(e);
			}
		}
	}
	
	@Override
	public void rollback(Object connectionHandle, boolean lastAutoCommit, String trxIdentifier) throws DataSourceException
	{
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		
		if(trxIdentifier != null){
			try {
				con.rollbackPrepared(trxIdentifier);
			}catch(Throwable e){
				throw new DataSourceException(e);
			}
			return;
		}
		
		try {
			con.rollback();
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}finally{
			try {
				con.setAutoCommit(lastAutoCommit);
			} catch (SQLException e) {
				throw new DataSourceException(e);
			}
			
		}
	}
	
	@Override
	public void dispose()
	{
		AlinousConnectionPool pool = this.connectionPool;

		try {
			pool.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void storeBinary(Object connectionHandle, InputStream stream, int length,
			String table, String blobColumn, WhereClause where,
			PostContext context, VariableRepository provider) throws ExecutionException
	{
		UpdateSentence sentence = new UpdateSentence();
		sentence.setTable(new TableIdentifier(table));
		TypeHelper helper = this.typeHelper.newHelper(false, sentence);
		
		StringBuffer buff = new StringBuffer();
		
		buff.append("UPDATE ");
		buff.append(table);
		buff.append(" SET ");
		buff.append(blobColumn);
		buff.append("=?");
		
		if(where != null && where.isReady(context, provider, null)){
			buff.append(" ");
			buff.append(where.extract(context, provider, null, null, helper));
		}
		
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		boolean lastAutoCommit = false;
		try {
			lastAutoCommit = con.getAutoCommit();
			if(lastAutoCommit == true){
				con.setAutoCommit(false);
			}
			
			PreparedStatement statement = con.prepareStatement(buff.toString());
			statement.setBinaryStream(1, stream, length);
			statement.executeUpdate();
			
			//con.commit();
		} catch (SQLException e) {
			throw new ExecutionException(e, "Failed in storing blob"); // i18n
		}finally{
			try {
				if(lastAutoCommit == true){
					con.setAutoCommit(lastAutoCommit);
				}
			} catch (SQLException e) {}
		}
		// UPDATE table SET blobColumn = ? WHERE...
		
	}
	

	public void readLargeObject(Object connectionHandle, String fileName, String table, String blobColumn, WhereClause where
			, PostContext context, VariableRepository provider)
			throws ExecutionException
	{
		// SELECT blobColumn FROM table WHERE ...
		SelectSentence sentence = new SelectSentence();
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		tableList.addTable(new TableIdentifier(table));
		from.setTableList(tableList);
		sentence.setFrom(from);
		
		TypeHelper helper = this.typeHelper.newHelper(false, sentence);
		
		StringBuffer buff = new StringBuffer();
		
		buff.append("SELECT ");
		buff.append(blobColumn);
		buff.append(" FROM ");
		buff.append(table);
		
		if(where != null && where.isReady(context, provider, null)){
			buff.append(" ");
			buff.append(where.extract(context, provider, null, null, helper));
		}
		
		OutputStream output = null;
		InputStream input = null;
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		boolean lastAutoCommit = false;
		try {
			lastAutoCommit = con.getAutoCommit();
			if(lastAutoCommit == true){
				con.setAutoCommit(false);
			}
			
			Statement statement = con.createStatement();
			ResultSet result = statement.executeQuery(buff.toString());
			result.next();
			
			output = new AlinousFileOutputStream(new AlinousFile(fileName));
			input = result.getBinaryStream(1);
			
			byte[] byteBuff = new byte[256];
			
			int n = 1;
			while(n > 0){
				n = input.read(byteBuff);
				
				if(n <= 0){
					break;
				}
				output.write(byteBuff, 0, n);
			}
			
			statement.close();
			
		} catch (SQLException e) {
			throw new ExecutionException(e, "Failed in reading blob"); // i18n
		} catch (IOException e) {
			throw new ExecutionException(e, "Failed in reading blob"); // i18n
		}finally{
			try {
				if(input != null){
					input.close();
				}
			} catch (IOException e) {}
			
			if(output != null){
				try {
					output.close();
				} catch (IOException e) {}
			}

			try {
				if(lastAutoCommit == true){
					con.setAutoCommit(lastAutoCommit);
				}
			} catch (SQLException e) {}
			
		}
	}
	
	@Override
	public void alterTable(Object connectionHandle,TableIdentifier table, IAlterAction action
			, boolean trxStarted, PostContext context, VariableRepository provider) throws ExecutionException, DataSourceException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("ALTER TABLE ");
		buff.append(table.extract(context, provider, null, null, this.typeHelper));
		
		if(action instanceof AlterAdd){
			buff.append(" ");
			buff.append(action.extract(context, provider, null, null, this.typeHelper));	
		}
		else if(action instanceof AlterDrop){
			buff.append(" ");
			buff.append(action.extract(context, provider, null, null, this.typeHelper));	
		}
		
		executeUpdateSQL(connectionHandle, buff.toString(), trxStarted);
	}

	public TypeHelper getTypeHelper()
	{
		return this.typeHelper;
	}

	public void createFunction(Object connectionHandle, PlSQLFunction func)
			throws DataSourceException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("CREATE ");
		
		if(func.isRelpace()){
			buff.append(" OR REPLACE ");
		}
		
		buff.append("FUNCTION ");
		
		buff.append(func.getFuncName());
		buff.append("(");
		
		boolean first = true;
		Iterator<ColumnTypeDescriptor> it = func.getTypeDesc().iterator();
		while(it.hasNext()){
			ColumnTypeDescriptor desc = it.next();
			
			if(first){
				first = false;
			}
			else{
				buff.append(",");
			}
			
			try {
				buff.append(desc.extract(null, null, null, null, null));
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new DataSourceException(e);
			}
		}
		
		buff.append(")\n");
		
		buff.append("RETURNS ");
		if(func.isSetof()){
			buff.append("SETOF ");
		}
		
		try {
			buff.append(func.getRetType().extract(null, null, null, null, null));
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new DataSourceException(e);
		}
		
		buff.append(" AS \n");
		
		buff.append("'");
		buff.append(func.getProgram().replaceAll("'", "''"));
		buff.append("'");
		
		buff.append("\n LANGUAGE ");
		
		buff.append("'");
		buff.append(func.getLanguage());
		buff.append("'");
		
		AlinousDebug.debugOut(core, buff.toString());
		executeUpdateSQL(connectionHandle, buff.toString(), false);
	}
	
	@Override
	public void createTrigger(Object connectionHandle, PlSQLTrigger trigger)
			throws DataSourceException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("CREATE TRIGGER ");
		
		buff.append(trigger.getTriggerName());
		buff.append(" ");
		
		if(trigger.getTiming() != null){
			buff.append(trigger.getTiming());
			buff.append(" ");
		}
		
		boolean first = true;
		Iterator<UpdateType> upIt = trigger.getUpdateTypes().iterator();
		while(upIt.hasNext()){
			UpdateType update = upIt.next();
			
			if(first){
				first = false;
			}else{
				buff.append(" OR ");
			}
			
			buff.append(update.getTypeName());
			
			boolean colFisrt = true;
			Iterator<String> colIt = update.getColList().iterator();
			while(colIt.hasNext()){
				String col = colIt.next();
				
				if(colFisrt){
					colFisrt = false;
					buff.append(" OF ");
				}
				else{
					buff.append(", ");
				}
				
				buff.append(col);
			}
		}
		
		buff.append(" ON ");
		buff.append(trigger.getTriggerTable());
		
		buff.append(" FOR EACH ");
		buff.append(trigger.getUpdateTarget());
		
		buff.append(" EXECUTE PROCEDURE ");
		buff.append(trigger.getFuncName());
		
		buff.append("(");
		
		try {
			buff.append(trigger.getFuncArguments().extract(null, null, null, null, null));
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new DataSourceException(e);
		}
		
		buff.append(")");
		
		AlinousDebug.debugOut(core, buff.toString());
		executeUpdateSQL(connectionHandle, buff.toString(), false);
	}
	
	@Override
	public void dropFunction(Object connectionHandle, PlSQLFunction func)
			throws DataSourceException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("DROP FUNCTION ");
		
		buff.append(func.getFuncName());
		buff.append("(");
		
		boolean first = true;
		Iterator<ColumnTypeDescriptor> it = func.getTypeDesc().iterator();
		while(it.hasNext()){
			ColumnTypeDescriptor desc = it.next();
			
			if(first){
				first = false;
			}
			else{
				buff.append(",");
			}
			
			try {
				buff.append(desc.extract(null, null, null, null, null));
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new DataSourceException(e);
			}
		}
		
		buff.append(")\n");
		
		AlinousDebug.debugOut(core, buff.toString());
		executeUpdateSQL(connectionHandle, buff.toString(), false);
	}
	
	@Override
	public void dropTrigger(Object connectionHandle, String triggerName,
			String table, String opt, boolean ifexixts) throws DataSourceException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("DROP TRIGGER ");
		
		if(ifexixts){
			buff.append("IF EXISTS ");
		}
		
		buff.append(triggerName);
		buff.append(" ON ");
		buff.append(table);
		
		if(opt != null){
			buff.append(" ");
			buff.append(opt);
		}
		
		
		AlinousDebug.debugOut(core, buff.toString());
		executeUpdateSQL(connectionHandle, buff.toString(), false);
	}

	@Override
	public void createIndex(Object connectionHandle, Identifier indexName,
			TableIdentifier table, List<ColumnIdentifier> columns,
			Identifier usingAlgo) throws DataSourceException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("CREATE INDEX ");
		
		buff.append(indexName.getName());
		buff.append(" ON ");
		buff.append(table.getTableName());
		
		if(usingAlgo != null){
			buff.append(" USING ");
			buff.append(usingAlgo.getName());
		}
		
		buff.append("(");
		
		boolean first = true;
		Iterator<ColumnIdentifier> it = columns.iterator();
		while(it.hasNext()){
			ColumnIdentifier colid = it.next();
			
			if(first){
				first = false;
			}
			else{
				buff.append(",");
			}
			buff.append(colid.getColumnName());
		}
		
		buff.append(")");
		
		AlinousDebug.debugOut(core, buff.toString());
		executeUpdateSQL(connectionHandle, buff.toString(), false);
	}
	
	@Override
	public void dropIndex(Object connectionHandle, Identifier indexName)
			throws DataSourceException {
		// TODO Auto-generated method stub
		StringBuffer buff = new StringBuffer();
		
		buff.append("DROP INDEX ");
		buff.append(indexName.getName());
		
		AlinousDebug.debugOut(core, buff.toString());
		executeUpdateSQL(connectionHandle, buff.toString(), false);
	}
	
	@Override
	public void clearPrecompile(Object connectionHandle, ISQLSentence sentence) {
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		PreparedStatement pstmt = null;
		
		pstmt = con.getPreparedStatement(sentence);
		if(pstmt != null){
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			con.clearPreparedStatement(sentence);
		}
		
		
	}
	
	@Override
	public boolean prepareTransaction(Object connectionHandle,
			String trxIdentifier) throws DataSourceException
	{
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		
		boolean result = false;
		
		try {
			result = con.prepareToransaction(trxIdentifier);
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}finally{
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				throw new DataSourceException(e);
			}
		}
		
		return result;
	}

	public boolean isResultUpper() {
		return resultUpper;
	}

	public void setResultUpper(boolean resultUpper) {
		this.resultUpper = resultUpper;
	}

	@Override
	public DataTable[] getDataTableList(Object connectionHandle) throws DataSourceException {
		ArrayList<DataTable> list = new ArrayList<DataTable>();
		ArrayList<String> tables = new ArrayList<String>();
		
		// enum tables
		String sql = "SELECT TABLENAME as tblname FROM PG_TABLES WHERE TABLENAME NOT LIKE'pg%' AND TABLENAME NOT LIKE'sql_%' ORDER BY TABLENAME";
		
		PostgreSqlConnection con = (PostgreSqlConnection)connectionHandle;
		Statement stmt = null;
		
		try {
			stmt = con.createStatement();
			
			stmt.execute(sql);
			
			ResultSet rs = stmt.getResultSet();
			while(rs.next()){
				String tableName = rs.getString(1); // metaData.getColumnName(1);
				
				tables.add(tableName);				
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
			throw new DataSourceException(e);
		}finally{
			try {
				stmt.close();
			} catch (SQLException ignore) {}
		}
		
		Iterator<String> it = tables.iterator();
		while(it.hasNext()){
			String key = it.next();
			DataTable dataTable = getDataTable(connectionHandle, key);
			
			if(dataTable != null){
				list.add(dataTable);
			}
		}
		
		return list.toArray(new DataTable[list.size()]);
	}


	
}
