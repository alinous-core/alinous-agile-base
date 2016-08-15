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
package org.alinous.datasrc;

import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousCore;
import org.alinous.datasrc.basic.ILogProvidor;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.DataTable;
import org.alinous.datasrc.types.PlSQLFunction;
import org.alinous.datasrc.types.PlSQLTrigger;
import org.alinous.datasrc.types.Record;
import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.plugin.IDatabaseInterceptor;
import org.alinous.script.ISQLSentence;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.FromClause;
import org.alinous.script.sql.GroupByClause;
import org.alinous.script.sql.LimitOffsetClause;
import org.alinous.script.sql.OrderByClause;
import org.alinous.script.sql.SelectColumns;
import org.alinous.script.sql.SetClause;
import org.alinous.script.sql.WhereClause;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.ddl.IAlterAction;
import org.alinous.script.sql.lock.ForUpdateClause;
import org.alinous.script.sql.other.ColumnList;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.VariableList;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.Identifier;

public class DataSrcConnection {
	private ILogProvidor logger;
	private Object connectionHandle;
	private IAlinousDataSource dataSource;
	
	private boolean lastAutoCommit;
	private boolean trxStarted;
	
	private IDatabaseInterceptor interceptor;
	
	private List<IConnectionClosedListner> closeListners = new ArrayList<IConnectionClosedListner>();
	
	public DataSrcConnection(IAlinousDataSource dataSource, ILogProvidor logger, PostContext context)
	{
		this.logger = logger;
		this.dataSource = dataSource;
		this.trxStarted = false;

		
		if(AlinousCore.DB_INTERCEPTOR_CLASS != null && !AlinousCore.DB_INTERCEPTOR_CLASS.equals("")){
			try {
				this.interceptor = (IDatabaseInterceptor) Class.forName(AlinousCore.DB_INTERCEPTOR_CLASS).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		/*
		if(this.context != null){
			if(this.context.getCurrentDataSrcConnection() != null){
				this.context.getCurrentDataSrcConnection().close();
			}
			
			this.context.setCurrentDataSrcConnection(this);
			this.context.setDataSrc(newDataSrc);
		}	
		*/
	}
	
	public void clearPreCompile(ISQLSentence sqlSentence)
	{
		this.dataSource.clearPrecompile(this.connectionHandle, sqlSentence);
	}
	
	// Connection
	public void connect() throws DataSourceException
	{
		this.connectionHandle = this.dataSource.connect();
		
		if(this.interceptor != null){
			this.interceptor.init();
		}
		
		this.lastAutoCommit = false;
		this.trxStarted = false;
		
		// test
	//	if(AlinousCore.debug()){
	//		PoolTester.getInstance().notifyOpened(connectionHandle);
	//	}
	}
	
	public void close()
	{
		// test
	//	if(AlinousCore.debug()){
	//		PoolTester.getInstance().notifyClosed(connectionHandle);
	//	}
		
		this.dataSource.close(this.connectionHandle);
		this.connectionHandle = null;
		
		Iterator<IConnectionClosedListner> it = this.closeListners.iterator();
		while(it.hasNext()){
			IConnectionClosedListner listner = it.next();
			listner.fireConnectionClosed(this);
		}
		
		this.closeListners.clear();
	}
	
	// DDL
	public void createTable(DataTable table) throws DataSourceException
	{
		this.dataSource.createTable(this.connectionHandle, table);
	}
	
	public void dropTable(String table) throws DataSourceException
	{
		this.dataSource.dropTable(this.connectionHandle, table);
	}
	
	public void createIndex(Identifier indexName, TableIdentifier table, List<ColumnIdentifier> columns, Identifier usingAlgo) throws DataSourceException
	{
		this.dataSource.createIndex(this.connectionHandle, indexName, table, columns, usingAlgo);
	}
	
	public void dropIndex(Identifier indexName) throws DataSourceException
	{
		this.dataSource.dropIndex(this.connectionHandle, indexName);
	}
	
	// Stored procedure
	public void createFunction(PlSQLFunction func) throws DataSourceException
	{
		this.dataSource.createFunction(this.connectionHandle, func);
	}
	public void dropFunction(PlSQLFunction func) throws DataSourceException
	{
		this.dataSource.dropFunction(this.connectionHandle, func);
	}
	
	// Trigger
	public void createTrigger(PlSQLTrigger trigger) throws DataSourceException
	{
		this.dataSource.createTrigger(this.connectionHandle, trigger);
	}
	public void dropTrigger(String triggerName, String table, String opt, boolean ifexixts) throws DataSourceException
	{
		this.dataSource.dropTrigger(this.connectionHandle, triggerName, table, opt, ifexixts);
	}
	
	
	public ILogProvidor getLogger() {
		return logger;
	}
	
	// Metadata
	public DataTable getDataTable(String tableName) throws DataSourceException
	{
		return this.dataSource.getDataTable(this.connectionHandle, tableName);
	}
	
	public DataTable[] getDataTableList() throws DataSourceException
	{
		return this.dataSource.getDataTableList(this.connectionHandle);
	}
	
	// DML Operations
	public void insert(Record record, String table, PostContext context, VariableRepository provider) throws DataSourceException
	{
		if(this.interceptor != null){
			this.interceptor.interceptInsert((Connection)this.connectionHandle, table);
		}
		this.dataSource.insert(this.connectionHandle, record, table, this.trxStarted, context, provider);
	}
	
	public void insert(List<Record> records, String table, PostContext context, VariableRepository provider) throws DataSourceException
	{
		if(this.interceptor != null){
			this.interceptor.interceptInsert((Connection)this.connectionHandle, table);
		}
		this.dataSource.insert(this.connectionHandle, records, table, this.trxStarted, context, provider);
	}
	
	public void insert(ColumnList cols, ArrayList<VariableList> valueLists, String table, PostContext context, VariableRepository provider)
				throws DataSourceException, ExecutionException
	{
		if(this.interceptor != null){
			this.interceptor.interceptInsert((Connection)this.connectionHandle, table);
		}
		this.dataSource.insert(connectionHandle, cols, valueLists, table, this.trxStarted, context, provider);
	}
	
	public void delete(String table, WhereClause where, PostContext context, VariableRepository provider, AdjustWhere adjWhere) throws DataSourceException, ExecutionException
	{
		this.dataSource.delete(this.connectionHandle, table, where, this.trxStarted, context, provider, adjWhere);
	}
	
	public void update(String table, SetClause set, WhereClause where, PostContext context, VariableRepository provider, AdjustWhere adjWhere, AdjustSet adjSet) throws DataSourceException, ExecutionException
	{
		this.dataSource.update(this.connectionHandle, table, set, where, this.trxStarted, context, provider, adjWhere, adjSet);
	}
	
	public List<Record> select(String distinct, SelectColumns columns, FromClause from, WhereClause where, GroupByClause groupBy,
			OrderByClause orderBy, LimitOffsetClause limit, ForUpdateClause forupdate
			, PostContext context, VariableRepository provider, AdjustWhere adjWhere) throws DataSourceException, ExecutionException
	{
		if(this.interceptor != null){
			this.interceptor.interceptSelect((Connection)this.connectionHandle, from);
		}
		return this.dataSource.select(this.connectionHandle, distinct, columns, from, where, groupBy, orderBy, limit, forupdate, context, provider, adjWhere);
	}
	
	// Transaction
	public void begin(int transactionIsolationLevel) throws DataSourceException
	{
		this.lastAutoCommit = this.dataSource.begin(this.connectionHandle, transactionIsolationLevel);
		this.trxStarted = true;
	}
	
	public boolean prepareTransaction(String trxIdentifier) throws DataSourceException
	{
		boolean result = this.dataSource.prepareTransaction(this.connectionHandle, trxIdentifier);
		this.trxStarted = false;
		this.lastAutoCommit = false;
		
		return result;
	}
	
	
	public void commit(String trxIdentifier) throws DataSourceException
	{
		this.dataSource.commit(this.connectionHandle, this.lastAutoCommit, trxIdentifier);
		this.trxStarted = false;
	}
	
	public void rollback(String trxIdentifier) throws DataSourceException
	{
		this.dataSource.rollback(this.connectionHandle, this.lastAutoCommit, trxIdentifier);
		this.trxStarted = false;
	}

	// blob functions
	public void readLargeObject(String fileName, String table, String blobColumn, WhereClause where
			, PostContext context, VariableRepository provider)
		throws ExecutionException
	{
		this.dataSource.readLargeObject(this.connectionHandle, fileName, table, blobColumn, where, context, provider);
	}
	
	public void storeBinary(InputStream stream, int length,
			String table, String blobColumn, WhereClause where
			, PostContext context, VariableRepository provider) throws ExecutionException
	{
		this.dataSource.storeBinary(this.connectionHandle, stream, length, table, blobColumn, where, context, provider);
	}
	
	// alter
	public void alterTable(TableIdentifier table, IAlterAction action
			, PostContext context, VariableRepository provider) throws ExecutionException, DataSourceException
	{
		this.dataSource.alterTable(this.connectionHandle, table, action, this.trxStarted, context, provider);
	}
	
	// debug function
	public void setOutSql(boolean outSql)
	{
		this.dataSource.setOutSql(outSql);
	}

	public Object getConnectionHandle()
	{
		return connectionHandle;
	}
	
	// getter and setter
	public TypeHelper getTypeHelper()
	{
		return this.dataSource.getTypeHelper();
	}
	
	public boolean isClosed()
	{
		if(this.connectionHandle == null){
			return true;
		}
		
		return false;
	}
	
	public void addConnectionCloseListner(IConnectionClosedListner listner)
	{
		this.closeListners.add(listner);
	}
	
	
}
