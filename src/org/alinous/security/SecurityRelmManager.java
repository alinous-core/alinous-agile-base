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
package org.alinous.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.AlinousConfig;
import org.alinous.datasrc.AlinousDataSourceManager;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.DataField;
import org.alinous.datasrc.types.DataTable;
import org.alinous.datasrc.types.Record;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.script.sql.FromClause;
import org.alinous.script.sql.SelectColumns;
import org.alinous.script.sql.WhereClause;
import org.alinous.script.sql.condition.AndExpression;
import org.alinous.script.sql.condition.TwoClauseExpression;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.TablesList;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.sql.statement.SQLStringConst;

public class SecurityRelmManager
{
	private AlinousDataSourceManager dataSourceManager;
	private AlinousConfig config;
	
	public SecurityRelmManager(AlinousDataSourceManager dataSourceManager, AlinousConfig config)
	{
		this.dataSourceManager = dataSourceManager;
		this.config = config;
	}
	
	public void initRelmTable() throws AlinousException
	{
		SecurityConfig secConfig = this.config.getSecurityConfig();
		
		if(secConfig.getRelmTable() == null){
			return;
		}
		
		String dataSrc = secConfig.getRelmDataSource();
		String dataTable = secConfig.getRelmTable().toUpperCase();
		String userColumn = secConfig.getRelmUsers();
		String passColumn = secConfig.getRelmPasswords();
		String rolesColumn = secConfig.getRelmRoles();
		
		if(dataSrc == null){
			//throw new AlinousException("<datastore> does not exist in <relm> tag."); // i18n
			return;
		}
		if(dataTable == null){
			throw new AlinousException("<table> does not exist in <relm> tag."); // i18n
		}
		if(userColumn == null){
			throw new AlinousException("<users> does not exist in <relm> tag."); // i18n
		}
		if(passColumn == null){
			throw new AlinousException("<passwords> does not exist in <relm> tag."); // i18n
		}
		if(rolesColumn == null){
			throw new AlinousException("<roles> does not exist in <relm> tag."); // i18n
		}
		
		// Check if database exists
		DataSrcConnection con = null;
		try {
			con = this.dataSourceManager.connect(dataSrc, null);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "failed to connect datasource " + dataSrc); // i18n
		}
		
		// Check BACKING_STATUS_TABLE
		DataTable tableObj = null;
		try {
			tableObj = con.getDataTable(dataTable);
		} catch (DataSourceException e) {
			con.close();
			throw new AlinousException(e, "Failed to get Metadata");
		}
		
		if(tableObj != null){
			con.close();
			return;
		}
		
		try{
			createTable(con, dataTable, userColumn, passColumn, rolesColumn);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to create the authentication table");
		}finally{
			con.close();
		}
	}
	
	private void createTable(DataSrcConnection con, String dataTable,
				String userColumn, String passColumn, String rolesColumn) throws DataSourceException
	{
		DataTable tableObj = new DataTable(dataTable);
		
		tableObj.addField(userColumn, DataField.TYPE_STRING, true, 128);
		tableObj.addField(passColumn, DataField.TYPE_STRING, true, 128);
		tableObj.addField(rolesColumn, DataField.TYPE_STRING);
		
		con.createTable(tableObj);
		
	}
	
	public List<Record> findRecords(String user, String password, PostContext context) throws AlinousException, DataSourceException
	{
		SecurityConfig secConfig = this.config.getSecurityConfig();
		if(secConfig == null){
			return new ArrayList<Record>();
		}
		
		String dataTable = secConfig.getRelmTable().toLowerCase();
		String userColumn = secConfig.getRelmUsers();
		String passColumn = secConfig.getRelmPasswords();
		
		if(dataTable == null){
			throw new AlinousException("<table> does not exist in <relm> tag."); // i18n
		}
		if(userColumn == null){
			throw new AlinousException("<users> does not exist in <relm> tag."); // i18n
		}
		if(passColumn == null){
			throw new AlinousException("<passwords> does not exist in <relm> tag."); // i18n
		}
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(userColumn, user);
		params.put(passColumn, password);
		
		return selectRecord(dataTable, params, context);
	}
	
	private List<Record> selectRecord(String tableName, Map<String, String> queryParams, PostContext context) throws DataSourceException, AlinousException
	{
		SecurityConfig secConfig = this.config.getSecurityConfig();
		String dataSrc = secConfig.getRelmDataSource();
		if(dataSrc == null){
			throw new AlinousException("<datastore> does not exist in <relm> tag."); // i18n
		}
		
		List<Record> recordList =null;
		DataSrcConnection con = this.dataSourceManager.connect(dataSrc, context);
		
		// from
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		TableIdentifier table = new TableIdentifier();
		table.setTableName(tableName);
		tableList.addTable(table);
		from.setTableList(tableList);
		
		SelectColumns columns = new SelectColumns();
		
		WhereClause where = createWhereClauseFromParamMap(queryParams);
		
		try{
			recordList = con.select(null, columns, from, where, null, null, null, null, null, null, null);
		}catch(DataSourceException e){
			throw e;
		}catch(ExecutionException e){
			throw e;
		}finally{
			con.close();
		}
		
		return recordList;
	}
	
	private WhereClause createWhereClauseFromParamMap(Map<String, String> queryParams)
	{
		WhereClause where = new WhereClause();
		AndExpression andExp = new AndExpression();
		Iterator<String> it = queryParams.keySet().iterator();
		while(it.hasNext()){
			String field = it.next();
			String val = queryParams.get(field);
			
			Identifier id = new Identifier();
			id.setName(field);
			
			SQLStringConst sqVal = new SQLStringConst();
			sqVal.setStr(val);
			
			TwoClauseExpression eqExp = new TwoClauseExpression();
			eqExp.setOpe("=");
			eqExp.setLeft(id);
			eqExp.setRight(sqVal);
			
			andExp.addExpressions(eqExp);
		}
		
		where.setExpression(andExp);
		
		return where;
	}
}
