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
import java.util.ArrayList;
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

public interface IAlinousDataSource {
	// Attributes
	public void setUri(String uri);
	public void setUser(String user);
	public void setPass(String pass);
	public void setMaxclients(int maxclients);
	
	public void setLogger(ILogProvidor logger, AlinousCore core);
	
	public void setResultUpper(boolean resultUpper);
	
	// init
	public void init(AlinousCore core) throws DataSourceException;
	
	// dispose
	public void dispose();
	
	// Connections
	public Object connect() throws DataSourceException;
	public void close(Object connectObj);
	
	// DDL
	public void createTable(Object connectionHandle, DataTable table) throws DataSourceException;
	public void dropTable(Object connectionHandle, String table) throws DataSourceException;
	public void createIndex(Object connectionHandle, Identifier indexName, TableIdentifier table, List<ColumnIdentifier> columns, Identifier usingAlgo) throws DataSourceException;
	public void dropIndex(Object connectionHandle, Identifier indexName) throws DataSourceException;
	
	// Stored procedure
	public void createFunction(Object connectionHandle, PlSQLFunction func) throws DataSourceException;
	public void dropFunction(Object connectionHandle, PlSQLFunction func) throws DataSourceException;
	
	// Trigger
	public void createTrigger(Object connectionHandle, PlSQLTrigger trigger) throws DataSourceException;
	public void dropTrigger(Object connectionHandle, String triggerName, String table, String opt, boolean ifexixts) throws DataSourceException;
	
	// MetaData handling
	public DataTable getDataTable(Object connectionHandle, String tableName) throws DataSourceException;
	public DataTable[] getDataTableList(Object connectionHandle) throws DataSourceException;
	
	// DML
	public void insert(Object connectionHandle, Record record, String table, boolean trxStarted, PostContext context, VariableRepository provider) throws DataSourceException;
	public void insert(Object connectionHandle, List<Record> records, String table, boolean trxStarted, PostContext context, VariableRepository provider) throws DataSourceException;
	public void insert(Object connectionHandle, ColumnList cols, ArrayList<VariableList> valueLists, String table, boolean trxStarted, PostContext context, VariableRepository provider) throws DataSourceException, ExecutionException;
	public void delete(Object connectionHandle, String table, WhereClause where, boolean trxStarted, PostContext context, VariableRepository provider, AdjustWhere adjWhere) throws DataSourceException, ExecutionException;
	public void update(Object connectionHandle, String table, SetClause set, WhereClause where, boolean trxStarted, PostContext context, VariableRepository provider, AdjustWhere adjWhere, AdjustSet adjSet) throws DataSourceException, ExecutionException;
	
	public List<Record> select(Object connectionHandle, String distinct, SelectColumns columns, FromClause from, WhereClause where,
			GroupByClause groupBy, OrderByClause orderBy, LimitOffsetClause limit, ForUpdateClause forupdate
			, PostContext context, VariableRepository provider, AdjustWhere adjWhere)
			throws DataSourceException, ExecutionException;
	
	// BLOB functions
	public void readLargeObject(Object connectionHandle, String fileName, String table, String blobColumn, WhereClause where
			, PostContext context, VariableRepository provider)
		throws ExecutionException;
	public void storeBinary(Object connectionHandle, InputStream stream, int length,
			String table, String blobColumn, WhereClause where, PostContext context, VariableRepository provider) throws ExecutionException;
			
	// Transaction
	public boolean begin(Object connectionHandle, int transactionIsolationLevel) throws DataSourceException;
	public boolean prepareTransaction(Object connectionHandle, String trxIdentifier) throws DataSourceException;
	public void commit(Object connectionHandle, boolean lastAutoCommit, String trxIdentifier) throws DataSourceException;
	public void rollback(Object connectionHandle, boolean lastAutoCommit, String trxIdentifier) throws DataSourceException;
	
	// alter
	public void alterTable(Object connectionHandle,TableIdentifier table, IAlterAction action
			, boolean trxStarted, PostContext context, VariableRepository provider)
				throws ExecutionException, DataSourceException;
	
	// Database creation
	public void createDatabase(Object connectionHandle, String dbName) throws DataSourceException;
	public void dropDatabase(Object connectionHandle, String dbName) throws DataSourceException;
	
	// Debug functions
	public void setOutSql(boolean outSql);
	
	public void executeUpdateSQL(Object connectionHandle, String sql, boolean trxStarted)
		throws DataSourceException;
	
	// get type Helper
	public TypeHelper getTypeHelper();
	
	// precompile
	public void clearPrecompile(Object connectionHandle, ISQLSentence sentence);
	
}
