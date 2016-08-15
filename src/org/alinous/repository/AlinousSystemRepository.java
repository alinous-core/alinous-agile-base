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
package org.alinous.repository;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.datasrc.AlinousDataSourceManager;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.DataField;
import org.alinous.datasrc.types.DataTable;
import org.alinous.datasrc.types.Record;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElement;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.DeleteSentence;
import org.alinous.script.sql.FromClause;
import org.alinous.script.sql.OrderByClause;
import org.alinous.script.sql.SelectColumns;
import org.alinous.script.sql.SelectSentence;
import org.alinous.script.sql.SetClause;
import org.alinous.script.sql.WhereClause;
import org.alinous.script.sql.condition.AndExpression;
import org.alinous.script.sql.condition.TwoClauseExpression;
import org.alinous.script.sql.lock.ForUpdateClause;
import org.alinous.script.sql.other.ColumnList;
import org.alinous.script.sql.other.SelectColumnElement;
import org.alinous.script.sql.other.SetPair;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.TablesList;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.sql.statement.SQLStringConst;
import org.alinous.script.sql.statement.SQLVariable;

/**
 * CREATE TABLE ALINOUS_ACCESS_REPOSITORY
 * 
 * @author iizuka
 *
 */
public class AlinousSystemRepository 
{
	public static final int HASH_VALUE = 2;
	
	// ALINOUS_VALUES
	public static final String VALUES_TABLE = "ALINOUS_VALUES".toLowerCase(); //$NON-NLS-1$
	public static final String SESSION_ID = "SESSION_ID".toLowerCase(); //$NON-NLS-1$
	public static final String MODULE_PATH = "MODULE_PATH".toLowerCase(); //$NON-NLS-1$
	public static final String FILE_PATH = "FILE_PATH".toLowerCase(); //$NON-NLS-1$
	public static final String NAME_PATH = "NAME_PATH".toLowerCase(); //$NON-NLS-1$
	public static final String TYPE = "TYPE".toLowerCase(); //$NON-NLS-1$
	public static final String VALUE_TYPE = "VALUE_TYPE".toLowerCase(); //$NON-NLS-1$
	public static final String VALUE = "VALUE".toLowerCase(); //$NON-NLS-1$
	public static final String CREATE_TIME = "CREATE_TIME".toLowerCase(); //$NON-NLS-1$
	public static final String LAST_FILE_PATH = "LAST_FILE_PATH".toLowerCase(); //$NON-NLS-1$
	public static final String LOOP_KEY = "LOOP_KEY".toLowerCase();
	
	// ALINOUS_FORM_VALUES
	public static final String FORM_VALUS_TABLE = "ALINOUS_FORM_VALUES".toLowerCase(); //$NON-NLS-1$
	public static final String FORM_ID = "FORM_ID".toLowerCase(); //$NON-NLS-1$
	public static final String VARIABLE_NAME = "VARIABLE_NAME".toLowerCase(); //$NON-NLS-1$
	
	// INNER TABLE STATUS
	public static final String INNER_STATUS_TABLE = "ALINOUS_INNER_STATUS".toLowerCase(); //$NON-NLS-1$
	
	// BACKING STATUS TABLE
	public static final String BACKING_STATUS_TABLE = "ALINOUS_BACKING_STATUS".toLowerCase(); //$NON-NLS-1$
	
	// SESSION TABLE
	public static final String SESSION_TABLE = "ALINOUS_SESSION_TABLE".toLowerCase(); //$NON-NLS-1$
	
	// LOCK TABLE
	public static final String LOCK_TABLE = "ALINOUS_LOCK_TABLE".toLowerCase(); //$NON-NLS-1$
	public static final String LOCK_TABLE_ID = "LOCK_ID".toLowerCase();
	public static final String LOCK_TABLE_USED = "LOCK_USED".toLowerCase();
	
	// GLOBAL LOCK
	public static final String GLOBAL_LOCK_TABLE = "ALINOUS_LOCK_GLOBAL_TABLE".toLowerCase(); //$NON-NLS-1$
	public static final String GLOBAL_LOCK_KEY = "GLOBAL_LOCK_KEY".toLowerCase();
	
	
	private SystemRepositoryConfig config;
	private AlinousDataSourceManager dataSourceManager;
	
	public AlinousSystemRepository(AlinousDataSourceManager dataSourceManager)
	{
		this.dataSourceManager = dataSourceManager;
	}
	
	public void install(SystemRepositoryConfig config) throws AlinousException
	{
		this.config = config;
		
		String dataSourceId = this.config.getSystemSrc();
		DataSrcConnection con = null;
		try {
			con = this.dataSourceManager.connect(dataSourceId, null);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to connect"); //$NON-NLS-1$
		}
		
		try {
			con.begin(Connection.TRANSACTION_READ_COMMITTED);
			
			// check metadata
			initValueCacheTable(con);
			initFormValueInfoTable(con);
			initInnerStatusTable(con);
			initBackingStatusTable(con);
			initSessionTable(con);
			initLockTable(con);
			initGlobalLockTable(con);
			
			con.commit(null);
		} catch (Throwable e) {
			throw new AlinousException(e, "Failed to create alinous table"); //$NON-NLS-1$
		}finally{
			con.close();
		}
	}
	
	private void initLockTable(DataSrcConnection con) throws AlinousException
	{
		DataTable dataTable = null;
		
		// Check BACKING_STATUS_TABLE 
		try {
			dataTable = con.getDataTable(LOCK_TABLE);
		} catch (DataSourceException e) {
			con.close();
			throw new AlinousException(e, "Failed to get Metadata");
		}
		
		//	if not checked, create metadata
		if(dataTable == null){
			dataTable = new DataTable(LOCK_TABLE);
			
			// table
			dataTable.addField(LOCK_TABLE_ID, DataField.TYPE_STRING, true, 128);
			dataTable.addField(LOCK_TABLE_USED, DataField.TYPE_TIMESTAMP, false);
			dataTable.addPrimaryKey(LOCK_TABLE_ID);
			
			try {
				con.createTable(dataTable);
			} catch (DataSourceException e) {
				con.close();
				throw new AlinousException(e,"Failed to CREATE TABLE "+ GLOBAL_LOCK_TABLE);
			}
		}
	}
	
	private void initGlobalLockTable(DataSrcConnection con) throws AlinousException
	{
		DataTable dataTable = null;
		
		// Check BACKING_STATUS_TABLE 
		try {
			dataTable = con.getDataTable(GLOBAL_LOCK_TABLE);
		} catch (DataSourceException e) {
			con.close();
			throw new AlinousException(e, "Failed to get Metadata");
		}
		//	if not checked, create metadata
		if(dataTable == null){
			dataTable = new DataTable(GLOBAL_LOCK_TABLE);
			
			// table
			dataTable.addField(GLOBAL_LOCK_KEY, DataField.TYPE_STRING, true, 128);
			dataTable.addPrimaryKey(GLOBAL_LOCK_KEY);
			
			// the only one record
			PostContext dummyContext = new PostContext(null, null);
			List<Record> records = new ArrayList<Record>();
			Record rec = new Record();
			rec.addFieldValue(GLOBAL_LOCK_KEY, "1", DataField.TYPE_STRING);
			records.add(rec);
			
			try {
				con.createTable(dataTable);
				insertRecord(dummyContext, con, GLOBAL_LOCK_TABLE, records);
			} catch (DataSourceException e) {
				con.close();
				throw new AlinousException(e,"Failed to CREATE TABLE "+ GLOBAL_LOCK_TABLE);
			}
		}
		
	}
	
	private void initSessionTable(DataSrcConnection con) throws AlinousException
	{
		DataTable dataTable = null;
		
		// Check BACKING_STATUS_TABLE 
		try {
			dataTable = con.getDataTable(SESSION_TABLE);
		} catch (DataSourceException e) {
			con.close();
			throw new AlinousException(e, "Failed to get Metadata");
		}
		
		//	if not checked, create metadata
		if(dataTable == null){
			dataTable = new DataTable(SESSION_TABLE);
			
			// table
			dataTable.addField(SESSION_ID, DataField.TYPE_STRING, true, 128);
			dataTable.addField(VALUE, DataField.TYPE_TEXT_STRING);
			dataTable.addField(CREATE_TIME, DataField.TYPE_TIMESTAMP);
			
			dataTable.addPrimaryKey(SESSION_ID);
			
			try {
				con.createTable(dataTable);
			} catch (DataSourceException e) {
				con.close();
				throw new AlinousException(e,"Failed to CREATE TABLE "+ SESSION_TABLE);
			}
		}
	}
	
	
	private void initBackingStatusTable(DataSrcConnection con) throws AlinousException
	{
		DataTable dataTable = null;
		
		// Check BACKING_STATUS_TABLE 
		try {
			dataTable = con.getDataTable(BACKING_STATUS_TABLE);
		} catch (DataSourceException e) {
			con.close();
			throw new AlinousException(e, "Failed to get Metadata");
		}
		
		//	if not checked, create metadata
		if(dataTable == null){
			dataTable = new DataTable(BACKING_STATUS_TABLE);

			dataTable.addField(SESSION_ID, DataField.TYPE_STRING, true, 128);
			dataTable.addField(MODULE_PATH, DataField.TYPE_TEXT_STRING, true, 255);
			dataTable.addField(FILE_PATH, DataField.TYPE_TEXT_STRING, true, 255);
			dataTable.addField(LAST_FILE_PATH, DataField.TYPE_TEXT_STRING);
			dataTable.addField(CREATE_TIME, DataField.TYPE_TIMESTAMP);
			
			dataTable.addPrimaryKey(SESSION_ID);
			dataTable.addPrimaryKey(MODULE_PATH);
			dataTable.addPrimaryKey(FILE_PATH);
			
			try {
				con.createTable(dataTable);
			} catch (DataSourceException e) {
				con.close();
				throw new AlinousException(e,"Failed to CREATE TABLE "+ BACKING_STATUS_TABLE);
			}
		}
		
	}
	
	private void initInnerStatusTable(DataSrcConnection con) throws AlinousException
	{
		DataTable dataTable = null;
		
		// Check INNER_STATUS_TABLE 
		try {
			dataTable = con.getDataTable(INNER_STATUS_TABLE);
		} catch (DataSourceException e) {
			con.close();
			throw new AlinousException(e, "Failed to get Metadata");
		}
		
		// if not checked, create metadata
		if(dataTable == null){
			dataTable = new DataTable(INNER_STATUS_TABLE);

			dataTable.addField(SESSION_ID, DataField.TYPE_STRING, true, 128);
			dataTable.addField(MODULE_PATH, DataField.TYPE_STRING, true, 255);
			dataTable.addField(FILE_PATH, DataField.TYPE_STRING);
			dataTable.addField(CREATE_TIME, DataField.TYPE_TIMESTAMP);
			
			dataTable.addPrimaryKey(SESSION_ID);
			dataTable.addPrimaryKey(MODULE_PATH);
			
			try {
				con.createTable(dataTable);
			} catch (DataSourceException e) {
				con.close();
				throw new AlinousException(e,"Failed to CREATE TABLE "+ INNER_STATUS_TABLE);
			}
		}
		
	}
	
	private void initFormValueInfoTable(DataSrcConnection con) throws AlinousException
	{
		DataTable dataTable = null;
		
		// Check FORM_VALUS_TABLE 
		try {
			dataTable = con.getDataTable(FORM_VALUS_TABLE);
		} catch (DataSourceException e) {
			con.close();
			throw new AlinousException(e, "Failed to get Metadata");
		}
		
		// if not checked, create metadata
		if(dataTable == null){
			dataTable = new DataTable(FORM_VALUS_TABLE);
			// table
			dataTable.addField(SESSION_ID, DataField.TYPE_STRING, true, 128);
			dataTable.addField(MODULE_PATH, DataField.TYPE_TEXT_STRING, true, 255);
			dataTable.addField(FILE_PATH, DataField.TYPE_STRING, true, 255);
			dataTable.addField(VALUE, DataField.TYPE_TEXT_STRING);
			dataTable.addField(CREATE_TIME, DataField.TYPE_TIMESTAMP);
			
			dataTable.addPrimaryKey(SESSION_ID);
			dataTable.addPrimaryKey(MODULE_PATH);
			dataTable.addPrimaryKey(FILE_PATH);
			
			try {
				con.createTable(dataTable);
			} catch (DataSourceException e) {
				con.close();
				throw new AlinousException(e,"Failed to CREATE TABLE "+ FORM_VALUS_TABLE);
			}
		}
	}
	
	private void initValueCacheTable(DataSrcConnection con) throws AlinousException
	{
		DataTable dataTable = null;
		
		// Check VALUES_TABLE 
		try {
			dataTable = con.getDataTable(VALUES_TABLE);
		} catch (DataSourceException e) {
			con.close();
			throw new AlinousException(e, "Failed to get Metadata");
		}
		
		// if not checked, create metadata
		if(dataTable == null){
			dataTable = new DataTable(VALUES_TABLE);

			dataTable.addField(SESSION_ID, DataField.TYPE_STRING, true, 32);
			dataTable.addField(MODULE_PATH, DataField.TYPE_TEXT_STRING, true, 64);
			dataTable.addField(FILE_PATH, DataField.TYPE_STRING, true, 64);
			dataTable.addField(LOOP_KEY, DataField.TYPE_STRING, true, 32);
			
			dataTable.addField(VALUE, DataField.TYPE_TEXT_STRING);
			dataTable.addField(CREATE_TIME, DataField.TYPE_TIMESTAMP);
			
			dataTable.addPrimaryKey(SESSION_ID);
			dataTable.addPrimaryKey(MODULE_PATH);
			dataTable.addPrimaryKey(FILE_PATH);
			dataTable.addPrimaryKey(LOOP_KEY);
			
			try {
				con.createTable(dataTable);
			} catch (DataSourceException e) {
				con.close();
				throw new AlinousException(e,"Failed to CREATE TABLE "+ VALUES_TABLE);// i18n
			}
		}
	}
	
	
	public List<Record> selectRecord(PostContext context, String tableName, Map<String, String> queryParams) throws DataSourceException, ExecutionException
	{
		List<Record> recordList =null;
		
		
		// from
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		TableIdentifier table = new TableIdentifier();
		table.setTableName(tableName);
		tableList.addTable(table);
		from.setTableList(tableList);
		
		// order by
		OrderByClause orderBy = new OrderByClause();
		ColumnList orderColumns = new ColumnList();
		SelectColumnElement columnIdentifier = new SelectColumnElement();
		columnIdentifier.setColumnName(CREATE_TIME);
		orderColumns.addColumns(columnIdentifier);
		orderBy.setColumnList(orderColumns);
		
		SelectColumns columns = new SelectColumns();
		
		VariableRepository valRepo = new VariableRepository();
		WhereClause where = createWhereClauseFromParamMap(queryParams, context, valRepo);
		
		DataSrcConnection con = context.getUnit().getConnectionManager().connect(config.getSystemSrc(), context);
		try{
			con.setOutSql(false);
			recordList = con.select(null, columns, from, where, null, orderBy, null, null, context, valRepo, null);
		}catch(DataSourceException e){
			throw e;
		}catch(ExecutionException e){
			throw e;
		}finally{
			//con.setOutSql(true);
			con.close();
			// 
			context.getUnit().getConnectionManager().clearCurrentDataSrc(context);
		}
		
		return recordList;
	}
	
	private WhereClause createWhereClauseFromParamMap(Map<String, String> queryParams,
			PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		return createWhereClauseFromParamMap(queryParams, context, valRepo, true);
	}
	
	private WhereClause createWhereClauseFromParamMap(Map<String, String> queryParams,
				PostContext context, VariableRepository valRepo, boolean useLoopGt) throws ExecutionException
	{
		WhereClause where = new WhereClause();
		AndExpression andExp = new AndExpression();
		Iterator<String> it = queryParams.keySet().iterator();
		while(it.hasNext()){
			String field = it.next();
			String val = queryParams.get(field);
			
			Identifier id = new Identifier();
			id.setName(field);
			
			//SQLStringConst sqVal = new SQLStringConst();
			//sqVal.setStr(val);
			
			TwoClauseExpression eqExp = new TwoClauseExpression();
			if(field.equals(AlinousSystemRepository.LOOP_KEY) && useLoopGt){
				eqExp.setOpe(">=");
			}else{
				eqExp.setOpe("=");
			}
			
			eqExp.setLeft(id);
			
			PathElement path = new PathElement(field);
			SQLVariable sqlVal = new SQLVariable();
			sqlVal.setPrefix("$");
			sqlVal.setPathElement(path);
			
			eqExp.setRight(sqlVal);
			
			andExp.addExpressions(eqExp);
			
			try {
				valRepo.putValue(field, val, IScriptVariable.TYPE_STRING, context);
			} catch (RedirectRequestException e) {
				e.printStackTrace();
			}
		}
		
		where.setExpression(andExp);
		
		return where;
	}
	
	public void updateRecord(PostContext context, DataSrcConnection con, String tableName, List<Record> records
			,Map<String, String> queryParams) throws ExecutionException, DataSourceException
	{
		VariableRepository valRepo = new VariableRepository();
		WhereClause where = createWhereClauseFromParamMap(queryParams, context, valRepo, false);
		SetClause set = new SetClause();
		
		if(records.isEmpty()){
			return;
		}
		
		Record rec = records.get(0);
		Iterator<String> it = rec.getMap().keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			SetPair p = new SetPair();
			
			ColumnIdentifier column = new ColumnIdentifier();
			column.setColumnName(key);
			p.setColumn(column);
			
			//SQLStringConst stringConst = new SQLStringConst();
			//stringConst.setStr(rec.getMap().get(key));
			//p.setValue(stringConst);
			
			// set
			String setValPath = "SETVAL." + key;
			PathElement pathroot = new PathElement("SETVAL");
			pathroot.setChild(new PathElement(key));
			
			SQLVariable sqlVal = new SQLVariable();
			sqlVal.setPrefix("$");
			sqlVal.setPathElement(pathroot);
			
			p.setValue(sqlVal);
			
			set.addSet(p);
			
			try {
				valRepo.putValue(setValPath, rec.getMap().get(key), IScriptVariable.TYPE_STRING, context);
			} catch (RedirectRequestException e) {
				e.printStackTrace();
			}
		}
		

		con.setOutSql(false);
		con.update(tableName, set, where, context, valRepo, null, null);
	}
	
	public void insertRecord(PostContext context, DataSrcConnection con, String tableName, List<Record> records)
				throws DataSourceException
	{
		try{
			VariableRepository dummy = new VariableRepository();
			
			con.setOutSql(false);
			con.insert(records, tableName, context, dummy);
		}catch(DataSourceException e){
			throw e;
		}
	}
	/*
	public void updateRecoed(DataSrcConnection con, String tableName, List<Record> records, Map<String, String> queryParams)
	{
		Iterator<Record> it = records.iterator();
		while(it.hasNext()){
			Record rec = it.next();
			
			SetClause set = new SetClause();
			set.addSet(new SetPair());
			
			WhereClause where;
			AdjustWhere adjWhere;
			AdjustSet adjSet;
			
			con.update(tableName, set, where, null, null, adjWhere, adjSet);
			//con.update(tableName, set, where, null, null, false, false);
			
		}
	}
	
	private SetClause createSet(Record rec)
	{
		SetClause set = new SetClause();
		
		return set;
	}*/
	
	public DataSrcConnection getConnection() throws DataSourceException
	{
		DataSrcConnection con = this.dataSourceManager.connect(config.getSystemSrc(), null);
		con.setOutSql(false);
		
		return con;
	}
	
	public void closeConnection(DataSrcConnection con)
	{
		con.setOutSql(false);
		con.close();
	}
	
	public boolean existsLock(PostContext context, DataSrcConnection con, String tableName, Map<String, String> queryParams)
		throws DataSourceException, ExecutionException
	{
		List<Record> list = selectForUpdateRecord(context, con, tableName, queryParams);
		
		if(list.isEmpty()){
			return false;
		}
		return true;
	}
	
	public List<String> existsLockMultiple(PostContext context, DataSrcConnection con, String tableName, Map<String, String> queryParams)
	throws DataSourceException, ExecutionException
	{
		List<Record> list = selectForUpdateRecord(context, con, tableName, queryParams);
		List<String> records = new ArrayList<String>();
		
		Iterator<Record> it = list.iterator();
		while(it.hasNext()){
			Record rec = it.next();
			
			String key = rec.getMap().get(AlinousSystemRepository.LOOP_KEY);
			records.add(key);
		}
		
		//
		//AlinousDebug.debugOut("@@@@@@@@@@@@@@@@@@@@@@@@@@@@ records locked : " + records.toString());
		
		return records;
	}
	
	public void deleteRecordWithoutLock(PostContext context, DataSrcConnection con, String tableName, Map<String, String> queryParams,
			DeleteSentence cacheDelete)
			throws DataSourceException, ExecutionException
	{
		VariableRepository valRepo = new VariableRepository();
		WhereClause where = createWhereClauseFromParamMap(queryParams, context, valRepo);
		
		try{
			context.getPrecompile().setSqlSentence(cacheDelete);
			con.setOutSql(false);
			con.delete("FROM " + tableName, where, context, valRepo, null);
			
			context.getPrecompile().setSqlSentence(null);
		}catch(DataSourceException e){
			throw e;
		}catch(ExecutionException e){
			throw e;
		}
	}
	
	public void deleteRecord(PostContext context, DataSrcConnection con, String tableName, Map<String, String> queryParams,
			SelectSentence cacheSelect, DeleteSentence cacheDelete)
			throws DataSourceException, ExecutionException
	{
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		TableIdentifier table = new TableIdentifier();
		table.setTableName(tableName);
		tableList.addTable(table);
		from.setTableList(tableList);
		
		VariableRepository valRepo = new VariableRepository();
		WhereClause where = createWhereClauseFromParamMap(queryParams, context, valRepo);
		
		try{
			context.getPrecompile().setSqlSentence(cacheSelect);
			selectForUpdateRecord(context, con, tableName, queryParams);
			
			context.getPrecompile().setSqlSentence(cacheDelete);
			con.setOutSql(false);
			con.delete("FROM " + tableName, where, context, valRepo, null);
			
			context.getPrecompile().setSqlSentence(null);
		}catch(DataSourceException e){
			throw e;
		}catch(ExecutionException e){
			throw e;
		}
	}
	
	// SELECT FOR UPDATE
	public List<Record> selectForUpdateRecord(PostContext context, DataSrcConnection con, String tableName, Map<String, String> queryParams)
		throws DataSourceException, ExecutionException
	{
		// from
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		TableIdentifier table = new TableIdentifier();
		table.setTableName(tableName);
		tableList.addTable(table);
		from.setTableList(tableList);
		
		// order by
		OrderByClause orderBy = new OrderByClause();
		ColumnList orderColumns = new ColumnList();
		SelectColumnElement columnIdentifier = new SelectColumnElement();
		columnIdentifier.setColumnName(CREATE_TIME);
		orderColumns.addColumns(columnIdentifier);
		orderBy.setColumnList(orderColumns);
		
		SelectColumns columns = new SelectColumns();
		
		VariableRepository valRepo = new VariableRepository();
		WhereClause where = createWhereClauseFromParamMap(queryParams, context, valRepo);
		
		// ForUpdate
		ForUpdateClause forUpdate = new ForUpdateClause();
		forUpdate.setType("UPDATE");
		
		List<Record> rec = null;
		try{
			con.setOutSql(false);
			rec = con.select(null, columns, from, where, null, orderBy, null, forUpdate, context, valRepo, null);
		}catch(DataSourceException e){
			con.close();
			throw e;
		}catch(ExecutionException e){
			con.close();
			throw e;
		}
		
		return rec;
	}
	
	public void cleanOldSessionData(AlinousCore core) throws DataSourceException, ExecutionException
	{
		long time = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		
		cal.add(Calendar.HOUR, -24);
		
		try {
			//con = getConnection();
			//con.setOutSql(true);
			
			String timeStr = new Timestamp(cal.getTimeInMillis()).toString();
			
			deleteOld4Table(VALUES_TABLE, CREATE_TIME, timeStr, core);
			deleteOld4Table(FORM_VALUS_TABLE, CREATE_TIME, timeStr, core);
			deleteOld4Table(INNER_STATUS_TABLE, CREATE_TIME, timeStr, core);
			deleteOld4Table(BACKING_STATUS_TABLE, CREATE_TIME, timeStr, core);
			deleteOld4Table(SESSION_TABLE, CREATE_TIME, timeStr, core);
			
			deleteOld4Table(LOCK_TABLE, LOCK_TABLE_USED, timeStr, core);
			
		}finally{

		}
		
	}
	
	private void deleteOld4Table(String tableName, String columnName, String timeStamp, AlinousCore core)
				throws ExecutionException, DataSourceException
	{
		WhereClause whereClause = buildWhere(timeStamp, columnName);
		FromClause from = buildFrom(tableName);
		
		AlinousSystemRepository sysrepo = core.getSystemRepository();
		AccessExecutionUnit unit = new AccessExecutionUnit(core.getModuleRepository(), "temp", sysrepo, dataSourceManager, core);
		
		
		PostContext context = new PostContext(core, unit);

		DataSrcConnection con = null;
		
		try{
			con = core.getDataSourceManager().connect(core.getConfig().getSystemRepositoryConfig().getSystemSrc(), context);
			context.setCurrentDataSrcConnection(con);
			
			con.delete(from.extract(null, null, null, null, con.getTypeHelper()), whereClause, context, null, null);
		}finally{
			con.close();
			context.dispose();
		}
	}
	
	private FromClause buildFrom(String tableName)
	{
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		TableIdentifier table = new TableIdentifier();
		table.setTableName(tableName);
		tableList.addTable(table);
		from.setTableList(tableList);
		
		return from;
	}
	
	private WhereClause buildWhere(String timeStamp, String columnName)
	{
		WhereClause whereClause = new WhereClause();
		
		Identifier id = new Identifier();
		id.setName(columnName);
		
		SQLStringConst sqVal = new SQLStringConst();
		sqVal.setStr(timeStamp);
		
		TwoClauseExpression eqExp = new TwoClauseExpression();
		eqExp.setOpe("<");
		eqExp.setLeft(id);
		eqExp.setRight(sqVal);
		
		whereClause.setExpression(eqExp);
		
		return whereClause;
	}
}
