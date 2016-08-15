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
package org.alinous.plugin.derby;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
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
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.ISQLSentence;
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
import org.alinous.script.sql.ddl.IAlterAction;
import org.alinous.script.sql.lock.ForUpdateClause;
import org.alinous.script.sql.other.ColumnList;
import org.alinous.script.sql.other.SelectColumnElement;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.TablesList;
import org.alinous.script.sql.other.VariableList;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.sql.statement.SQLNumericConst;
import org.alinous.script.sql.statement.SQLStringConst;
import org.apache.commons.pool.impl.StackObjectPool;

public class DerbyDataSource implements IAlinousDataSource
{
	private Driver driver;

	private String user;
	private String pass;
	private String uri;

	private DerbyConnectionFactory factory;
	private StackObjectPool connectionPool;
	
	private boolean outSql = true;
	
	private TypeHelper typeHelper;

	private ILogProvidor logger;

	private Map<Connection, Statement> batchedStatementMap = new HashMap<Connection, Statement>();
	
	private AlinousCore core;

	public void init(AlinousCore core) throws DataSourceException
	{
		try {
			driver = (Driver) Class.forName(
					"org.apache.derby.jdbc.ClientDriver").newInstance();
		} catch (ClassNotFoundException e) {
			throw new DataSourceException(e);
		} catch (InstantiationException e) {
			throw new DataSourceException(e);
		} catch (IllegalAccessException e) {
			throw new DataSourceException(e);
		}
		
		this.factory = new DerbyConnectionFactory(this.driver, this.user, this.pass, this.uri);
		
		this.connectionPool = new StackObjectPool(this.factory);
		
		this.typeHelper = new TypeHelper(this);
		
		this.core = core;
	}

	public Object connect() throws DataSourceException
	{
		/*
		Properties info = new Properties();
		if (this.user != null && !this.user.equals("")) {
			info.put("user", this.user);
		}
		if (this.pass != null && !this.pass.equals("")) {
			info.put("password", this.pass);
		}

		Connection con = null;
		try {
			con = this.driver.connect(this.uri, info);
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}
		*/
		Connection con;
		try {
			con = (Connection) this.connectionPool.borrowObject();
		} catch (Exception e) {
			throw new DataSourceException(e);
		}
		
		return con;
	}

	public void close(Object connectObj)
	{
		if (connectObj == null) {
			return;
		}

		/*
		Connection con = (Connection) connectObj;
		try {
			con.close();
		} catch (SQLException e) {
		}
		 */
		
		Connection con = (Connection) connectObj;
		this.batchedStatementMap.remove(con);
		
		try {
			this.connectionPool.returnObject(connectObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean begin(Object connectionHandle, int transactionIsolationLevel) throws DataSourceException
	{
		Connection con = (Connection) connectionHandle;
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

		Statement batchStmt = null;
		try {
			batchStmt = con.createStatement();
			this.batchedStatementMap.put(con, batchStmt);
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}

		return autoCommit;
	}

	public void commit(Object connectionHandle, boolean lastAutoCommit, String trxIdentifier)
			throws DataSourceException
	{
		Connection con = (Connection) connectionHandle;

		Statement batchStmt = this.batchedStatementMap.get(con);
		if (batchStmt != null) {
			try {
				batchStmt.executeBatch();
			} catch (SQLException e) {
				try {
					con.rollback();
				} catch (SQLException e1) {
				}

				try {
					con.setAutoCommit(lastAutoCommit);
				} catch (SQLException ignore) {
				}

				try {
					batchStmt.close();
				} catch (SQLException e1) {
				}

				// this.batchStmt = null;
				this.batchedStatementMap.remove(con);

				throw new DataSourceException(e);
			}
		}

		try {
			con.commit();
		} catch (SQLException e) {
			throw new DataSourceException(e);
		} finally {
			try {
				batchStmt.close();
			} catch (SQLException e1) {
			}

			try {
				con.setAutoCommit(lastAutoCommit);
			} catch (SQLException ignore) {
			}

			this.batchedStatementMap.remove(con);
		}
	}

	public void rollback(Object connectionHandle, boolean lastAutoCommit, String trxIdentifier)
			throws DataSourceException
	{
		if(trxIdentifier != null){
				
			
			return;
		}
		
		Connection con = (Connection) connectionHandle;

		Statement batchStmt = this.batchedStatementMap.get(con);
		if (batchStmt != null) {
			try {
				batchStmt.close();
			} catch (SQLException e) {
			}
			// this.batchStmt = null;
			this.batchedStatementMap.remove(con);
		}

		try {
			con.rollback();
		} catch (SQLException e) {
			throw new DataSourceException(e);
		} finally {
			try {
				con.setAutoCommit(lastAutoCommit);
			} catch (SQLException ignore) {
			}

		}
	}

	public void createTable(Object connectionHandle, DataTable table)
			throws DataSourceException
	{
		List<DataField> fields = new LinkedList<DataField>();
		List<String> indexes = new LinkedList<String>();

		List<String> keys = table.getFields();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String fld = it.next();
			DataField df = table.getDataField(fld);

			fields.add(df);

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
		while (itDf.hasNext()) {
			DataField fld = itDf.next();

			if (first) {
				first = false;
			} else {
				sqlString.append(",\n");
			}

			sqlString.append(fld.getName());
			sqlString.append(" ");
			sqlString.append(getSQLType(fld.getType()));
			
			// default
			if(fld.getDefaultValue() != null){
				sqlString.append(" ");
				sqlString.append(fld.getDefaultString());
			}
		}

		// primary key
		if (indexes.size() > 0) {
			sqlString.append(",\n");
			sqlString.append("PRIMARY KEY(");

			first = true;
			it = indexes.iterator();
			while (it.hasNext()) {
				String idxFld = it.next();

				if (first) {
					first = false;
				} else {
					sqlString.append(", ");
				}

				sqlString.append(idxFld);
			}

			sqlString.append(")");
		}

		sqlString.append("\n)\n");

		if (this.logger != null && this.outSql) {
			this.logger.reportInfo(sqlString.toString());
		}

		executeUpdateSQL(connectionHandle, sqlString.toString(), false);
	}

	private String getSQLType(String type)
	{
		if (type.equals(DataField.TYPE_STRING)) {
			return "VARCHAR(256)";
		}
		if (type.equals(DataField.TYPE_TEXT_STRING)) {
			return "VARCHAR(4096)";
		}
		if (type.equals(DataField.TYPE_INTEGER)) {
			return "INTEGER";
		}
		if (type.equals(DataField.TYPE_DOUBLE)) {
			return "DOUBLE";
		}
		if (type.equals(DataField.TYPE_TIMESTAMP)) {
			return "TIMESTAMP";
		}
		if (type.equals(DataField.TYPE_DATE)) {
			return "DATE";
		}

		return "VARCHAR(256)";
	}

	public void dropTable(Object connectionHandle, String table)
			throws DataSourceException
	{
		// Make SQL String
		StringBuffer sqlString = new StringBuffer();
		sqlString.append("DROP TABLE ");
		sqlString.append(table);

		executeUpdateSQL(connectionHandle, sqlString.toString(), false);
	}

	public void insert(Object connectionHandle, Record record, String table,
			boolean trxStarted, PostContext context, VariableRepository provider) throws DataSourceException
	{
		String sql = makeInsertString(connectionHandle, record, table);

		// out string
		if (this.outSql) {
			AlinousDebug.println(sql);
		}

		executeUpdateSQL(connectionHandle, sql, trxStarted);
	}

	private String makeInsertString(Object connectionHandle, Record record, String table) throws DataSourceException
	{
		StringBuffer sql = new StringBuffer();

		sql.append("INSERT INTO ");
		sql.append(table);

		sql.append(" (");
		boolean first = true;
		Iterator<String> itFld = record.getMap().keySet().iterator();
		while (itFld.hasNext()) {
			String field = itFld.next();

			if (first) {
				first = false;
			} else {
				sql.append(", ");
			}

			sql.append(field);
		}
		sql.append(")");

		sql.append(" VALUES ");

		outRecordValue(connectionHandle, record, sql, table);

		return sql.toString();
	}

	public void insert(Object connectionHandle, List<Record> records,
			String table, boolean trxStarted, PostContext context, VariableRepository provider) throws DataSourceException
	{
		/*
		 * StringBuffer buff = new StringBuffer();
		 * 
		 * Iterator<Record> it = records.iterator(); while(it.hasNext()){
		 * Record rec = it.next();
		 * 
		 * String sql = makeInsertString(rec, table);
		 * 
		 * buff.append(sql); buff.append("\n"); //insert(connectionHandle, rec,
		 * table); }
		 *  // out string if(this.outSql){
		 * AlinousDebug.println(buff.toString()); }
		 * executeUpdateSQL(connectionHandle, buff.toString(), trxStarted);
		 */

		StringBuffer sql = new StringBuffer();

		sql.append("INSERT INTO ");
		sql.append(table);

		sql.append(" (");
		boolean first = true;
		Iterator<String> itFld = records.get(0).getMap().keySet().iterator();
		while (itFld.hasNext()) {
			String field = itFld.next();

			if (first) {
				first = false;
			} else {
				sql.append(", ");
			}

			sql.append(field);
		}
		sql.append(")");

		sql.append(" VALUES ");

		first = true;
		Iterator<Record> it = records.iterator();
		while (it.hasNext()) {
			Record rec = it.next();
			StringBuffer eachSql = new StringBuffer(sql);

			outRecordValue(connectionHandle, rec, eachSql, table);

			executeUpdateSQL(connectionHandle, eachSql.toString(), trxStarted);
		}

		// out string
		// AlinousDebug.debugOut(sql.toString());
		// executeUpdateSQL(connectionHandle, sql.toString());*/
	}

	public void insert(Object connectionHandle, ColumnList cols,
			ArrayList<VariableList> valueLists, String table, boolean trxStarted
			, PostContext context, VariableRepository provider)
			throws DataSourceException, ExecutionException
	{
		InsertSentence sentence = new InsertSentence();
		sentence.setCols(cols);
		TableIdentifier tbl = new TableIdentifier(table);
		sentence.setTbl(tbl);
		
		TypeHelper helper = this.typeHelper.newHelper(false, sentence);
		
		StringBuffer sql = new StringBuffer();	
		
		sql.append("INSERT INTO ");
		sql.append(table);

		// col definition
		if (cols != null && cols.isReady(context, provider, null)) {
			sql.append(" ( ");

			boolean first = true;
			Iterator<SelectColumnElement> it = cols.iterator();
			while (it.hasNext()) {
				SelectColumnElement element = it.next();

				if (first) {
					first = false;
				} else {
					sql.append(", ");
				}
				
				try {
					sql.append(element.extract(context, provider, null, null, helper));
				} catch (RedirectRequestException e) {
					e.printStackTrace();
				}
				String columnName = element.getColumnName().extract(context, provider, null, null, helper);
				
				String fldType = helper.getDataFieldType(columnName, context);
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

		boolean first = true;
		Iterator<VariableList> valListIt = valueLists.iterator();
		while (valListIt.hasNext()) {
			VariableList valList = valListIt.next();

			if (first) {
				first = false;
			} else {
				sql.append(", ");
			}
			
			valList = getTypeChangedList(connectionHandle, table, valList);
			
			helper.resetCount();
			sql.append(valList.extract(context, provider, null, null, helper));
		}

		if (this.outSql) {
			AlinousDebug.debugOut(core, sql.toString());
		}
		executeUpdateSQL(connectionHandle, sql.toString(), trxStarted);
	}
	
	private VariableList getTypeChangedList(Object connectionHandle, String table,
			VariableList valList) throws DataSourceException
	{
		DataTable dataTable = this.getDataTable(connectionHandle, table);
		
		VariableList newVariableList = new VariableList();
		
		Iterator<ISQLStatement> it = valList.iterator();
		int cnt = 0;
		while(it.hasNext()){
			ISQLStatement stmt = it.next();
			DataField fld = dataTable.getField(cnt);
			
			if(fld.getType().equals(DataField.TYPE_INTEGER) && stmt instanceof SQLStringConst){
				SQLNumericConst numConst = new SQLNumericConst();
				numConst.setNumber(((SQLStringConst)stmt).getStr());
				newVariableList.addValues(numConst);
				cnt++;
				continue;
			}
			
			newVariableList.addValues(stmt);
			
			cnt++;
		}
		
		return newVariableList;
		/*
		DataTable dataTable = this.getDataTable(connectionHandle, table);
		DataField fld = dataTable.getField(cnt);
		if(fld.getType().equals(DataField.TYPE_STRING) ||
				fld.getType().equals(DataField.TYPE_TEXT_STRING) ||
				fld.getType().equals(DataField.TYPE_TIMESTAMP)){
			
			sql.append(element.extract());
			
		}else{
			String extracted = element.extract();
			
			if(extracted.startsWith("'") && extracted.endsWith("'")){
				sql.append(extracted.substring(1, extracted.length() - 1));
			}
			
		}
		
		cnt++;*/
	}
	
	private void outRecordValue(Object connectionHandle, Record record, StringBuffer sql, String table) throws DataSourceException
	{
		// change Type of record
		record = changeType(connectionHandle, table, record);
		
		sql.append("(");

		boolean first = true;
		Map<String, String> map = record.getMap();
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			if (first) {
				first = false;
			} else {
				sql.append(", ");
			}
			String fldName = it.next();
			String val = map.get(fldName);
			String type = record.getFieldType(fldName);
			
			if(val != null && type != null && (
					type.equals(DataField.TYPE_INTEGER) || type.equals(DataField.TYPE_DOUBLE))){
				sql.append(val);
			}
			else if (val != null) {
				sql.append("'");
				sql.append(val);
				sql.append("'");
			} else {
				sql.append("NULL");
			}
		}

		sql.append(")");
	}
	
	private Record changeType(Object connectionHandle, String table, Record record) throws DataSourceException
	{
		DataTable dataTable = this.getDataTable(connectionHandle, table);
		
		Iterator<String> it = record.getMap().keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
						
			DataField fld = dataTable.getDataField(key);
			if(fld.getType().equals(DataField.TYPE_DOUBLE)){
				record.setFieldType(key, DataField.TYPE_DOUBLE);
			}
			else if(fld.getType().equals(DataField.TYPE_INTEGER)){
				record.setFieldType(key, DataField.TYPE_INTEGER);
			}
		}
		
		return record;
	}

	public void delete(Object connectionHandle, String fromStr, WhereClause where
			, boolean trxStarted, PostContext context, VariableRepository provider, AdjustWhere adjWhere)
						throws DataSourceException, ExecutionException
	{
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
	
	public void update(Object connectionHandle, String table, SetClause set, WhereClause where
			, boolean trxStarted, PostContext context, VariableRepository provider, AdjustWhere adjWhere, AdjustSet adjSet)
									throws DataSourceException, ExecutionException
	{
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
	

	public void executeUpdateSQL(Object connectionHandle, String sql,
			boolean trxStarted) throws DataSourceException
	{
		Connection con = (Connection) connectionHandle;

		if (trxStarted) {
			try {
				executeAddBatch(con, sql);
			} catch (SQLException e) {
				AlinousDebug.debugOut(core, sql);
				throw new DataSourceException(e);
			}
			return;
		}

		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			AlinousDebug.debugOut(core, sql);
			throw new DataSourceException(e);
		}
	}

	private void executeAddBatch(Connection con, String sql)
			throws SQLException
	{
		Statement batchStmt = this.batchedStatementMap.get(con);
		if (batchStmt == null) {
			batchStmt = con.createStatement();
			this.batchedStatementMap.put(con, batchStmt);
		}

		batchStmt.addBatch(sql);
	}

	public DataTable getDataTable(Object connectionHandle, String tableName) throws DataSourceException
	{
		DataTable dataTable = null;
		Connection con = (Connection)connectionHandle;
		
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
				
				if(!tableName.equals(tbl.toUpperCase())){
					continue;
				}
				// Create DataTable object
				dataTable = new DataTable();
				
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
		ResultSet trs = metaData.getColumns(null, null, tableName, null);
		while(trs.next()){
			String columnName = trs.getString("COLUMN_NAME");
			String columnType = trs.getString("TYPE_NAME");
			
			DataField fld = new DataField();
			fld.setName(columnName);
			
			// setType
			if(columnType.toUpperCase().equals("INTEGER")){
				fld.setType(DataField.TYPE_INTEGER);
			}
			else if(columnType.toUpperCase().equals("DOUBLE")){
				fld.setType(DataField.TYPE_DOUBLE);
			}
			else{
				fld.setType(DataField.TYPE_STRING);
			}
			
			dataTable.addField(fld);
		}

	}
	
	
	public List<Record> select(Object connectionHandle, String distinct, SelectColumns columns, FromClause from, WhereClause where,
			GroupByClause groupBy, OrderByClause orderBy, LimitOffsetClause limit, ForUpdateClause forupdate
			, PostContext context, VariableRepository provider, AdjustWhere adjWhere)
			throws DataSourceException, ExecutionException
	{
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
		
		sql.append(from.extract(context, provider, adjWhere, null, helper));
		
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
		
		//if(limit != null && limit.isReady()){
		//	sql.append(" ");
		//	sql.append(limit.extract());
		//}
		
		/*
		if(forupdate != null && forupdate.isReady(context, provider, adjWhere)){
			sql.append(" ");
			sql.append(forupdate.extract(context, provider, adjWhere, null, helper));
		}*/
		
		// out String
		if(this.outSql){
			AlinousDebug.debugOut(core, sql.toString());
		}
		List<Record> tmp = executeSelectSQL(connectionHandle, sql.toString(), limit,
				context, provider, adjWhere, helper);
		return tmp;
	}
	
	private List<Record> executeSelectSQL(Object connectionHandle, String sql,  LimitOffsetClause limit
			, PostContext context, VariableRepository provider, AdjustWhere adjWhere, TypeHelper helper)
			throws DataSourceException, ExecutionException
	{
		Connection con = (Connection)connectionHandle;
		Statement stmt = null;
		List<Record> retList = new LinkedList<Record>();
		
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			stmt.execute(sql);
			
			ResultSet rs = stmt.getResultSet();
			
			ResultSetMetaData metaData = rs.getMetaData();
			
			if(limit != null && limit.isReady(context, provider, adjWhere)){
				fetchWithOffset(rs, metaData, retList, limit, context, provider, adjWhere, helper);
			}
			else{
				while(rs.next()){
					int cnt = metaData.getColumnCount();
					Record rec = new Record();
					for(int i = 0; i < cnt; i++){
						String colName = metaData.getColumnName(i + 1).toUpperCase();
						String value = rs.getString(i + 1);
						
						int colType = metaData.getColumnType(i + 1);
						rec.addFieldValue(colName, value, colType);
					}
					
					retList.add(rec);
				}
			}

		} catch (SQLException e) {
			throw new DataSourceException(e);
		}finally{
			try {
				stmt.close();
			} catch (SQLException ignore) {}
		}
		
		return retList;
	}
	
	private void fetchWithOffset(ResultSet rs, ResultSetMetaData metaData, List<Record> retList, LimitOffsetClause limit
			, PostContext context, VariableRepository provider, AdjustWhere adjWhere, TypeHelper helper) throws ExecutionException, SQLException
	{
		ISQLStatement limitStmt = limit.getLimit();
		ISQLStatement offsetStmt = limit.getOffset();
		
		int nLimit = 0, nOffset = 0;
		if(limitStmt != null && limitStmt.isReady(context, provider, adjWhere)){
			String str = limitStmt.extract(context, provider, adjWhere, null, helper);
			nLimit = Integer.parseInt(str);
		}
		if(offsetStmt != null && offsetStmt.isReady(context, provider, adjWhere)){
			String str = offsetStmt.extract(context, provider, adjWhere, null, helper);
			nOffset = Integer.parseInt(str);
		}
		
		if(offsetStmt != null){
			rs.absolute(nOffset);
		}
		
		int count = 0;
		while(rs.next()){
			if(count >= nLimit){
				break;
			}
			count++;
			
			int cnt = metaData.getColumnCount();
			Record rec = new Record();
			for(int i = 0; i < cnt; i++){
				String colName = metaData.getColumnName(i + 1).toUpperCase();
				String value = rs.getString(i + 1);
				
				int colType = metaData.getColumnType(i + 1);
				rec.addFieldValue(colName, value, colType);
			}
			
			retList.add(rec);
		}
		
	}
	
	
	public void setLogger(ILogProvidor logger, AlinousCore core)
	{
		this.logger = logger;

	}

	public void setOutSql(boolean outSql)
	{
		this.outSql = outSql;
	}

	public void setPass(String pass)
	{
		this.pass = pass;
	}

	public void setUri(String uri)
	{
		this.uri = uri;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public void dispose()
	{
		StackObjectPool pool = this.connectionPool;
		this.connectionPool = null;
		
		pool.clear();
		
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
		
		Connection con = (Connection)connectionHandle;
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
		Connection con = (Connection)connectionHandle;
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

	public void createDatabase(Object connectionHandle, String dbName)
			throws DataSourceException {
		
	}

	public void dropDatabase(Object connectionHandle, String dbName)
			throws DataSourceException {
		
	}

	public void createFunction(Object connectionHandle, PlSQLFunction func)
			throws DataSourceException
	{
		
	}

	public void createTrigger(Object connectionHandle, PlSQLTrigger trigger)
			throws DataSourceException
	{
		
	}

	public void dropFunction(Object connectionHandle, PlSQLFunction func)
			throws DataSourceException
	{
		
	}

	public void dropTrigger(Object connectionHandle, String triggerName,
			String table, String opt, boolean ifexixts) throws DataSourceException
	{
		
	}

	@Override
	public void createIndex(Object connectionHandle, Identifier indexName,
			TableIdentifier table, List<ColumnIdentifier> columns,
			Identifier usingAlgo)
	{
		
	}

	@Override
	public void clearPrecompile(Object connectionHandle, ISQLSentence sentence) {
		
	}

	@Override
	public void setMaxclients(int maxclients) {
		
	}

	@Override
	public boolean prepareTransaction(Object connectionHandle,
			String trxIdentifier) throws DataSourceException {
		throw new DataSourceException("Not supported");
	}

	@Override
	public void setResultUpper(boolean resultUpper) {
		
	}

	@Override
	public DataTable[] getDataTableList(Object connectionHandle) throws DataSourceException {
		return null;
	}

	@Override
	public void dropIndex(Object connectionHandle, Identifier indexName)
			throws DataSourceException {
		// TODO Auto-generated method stub
		
	}



}
