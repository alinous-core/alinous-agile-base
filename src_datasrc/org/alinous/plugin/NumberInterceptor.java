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
package org.alinous.plugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.exception.MaxRecordsException;
import org.alinous.datasrc.types.Record;
import org.alinous.script.sql.FromClause;
import org.alinous.script.sql.ISQLScriptObject;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.TablesList;

public abstract class NumberInterceptor implements IDatabaseInterceptor
{
	protected abstract int getMaxRecord();
	protected abstract boolean isUnlimitedTable(String tableName);
	
	public void init() {
		
	}

	public void interceptInsert(Connection con, String table) throws MaxRecordsException {
		
		handleTable(con, table, true);

	}
	
	public void interceptSelect(Connection con, FromClause from)
			throws MaxRecordsException
	{

		TablesList tableList = from.getTableList();
		
		Iterator<ISQLScriptObject> it = tableList.iterator();
		while(it.hasNext()){
			ISQLScriptObject sqlObj = it.next();
			
			if(sqlObj instanceof TableIdentifier){
				String table = ((TableIdentifier)sqlObj).getTableName();
				handleTable(con, table, false);
			}
		}
	}
	
	private void handleTable(Connection con, String table, boolean insert) throws MaxRecordsException
	{
		int numRecords = getNumber(con, table, insert);
		
		if(isUnlimitedTable(table)){
			return;
		}
		
		
		if(numRecords > getMaxRecord()){
			throw new MaxRecordsException("You cannot add records into table '" + table + "' more than "
					+ getMaxRecord() + ".");
		}
	}
	
	private int getNumber(Connection con, String table, boolean insert)
	{
		String sql = "SELECT COUNT(*) AS CNT FROM " + table.toUpperCase();
		int numRecords = 0;
		try {
			List<Record> recList = executeSelectSQL(con, sql);
			
			String resultStr = recList.get(0).getFieldValue("CNT");
			numRecords = Integer.parseInt(resultStr);
			
		} catch (DataSourceException e) {
			e.printStackTrace();
			return 0;
		}
		
		return numRecords;
	}

	private List<Record> executeSelectSQL(Object connectionHandle, String sql) throws DataSourceException
	{
		Connection con = (Connection)connectionHandle;
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
					String colName = metaData.getColumnName(i + 1).toUpperCase();
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

}
