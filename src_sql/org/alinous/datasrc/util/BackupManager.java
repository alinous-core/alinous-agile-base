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
package org.alinous.datasrc.util;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
import org.alinous.csv.CsvWriter;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.expections.AlinousException;

public class BackupManager
{
	public void backup(AlinousCore core, DataSrcConnection dataSrc, String tableName, OutputStream stream) throws AlinousException
	{
		Object handle = dataSrc.getConnectionHandle();
		if(!(handle instanceof Connection)){
			throw new AlinousException("Cannot backup this type of datasource : " + handle.getClass());
		}
		
		int limit = 10000;
		int maxCount = 0;
		
		
		StringBuffer buff = new StringBuffer();
		buff.append("SELECT * FROM ");
		buff.append(tableName);
		buff.append(" LIMIT 1;");
		
		AlinousDebug.debugOut(core, buff.toString());
		
		Connection con = (Connection)handle;
		Statement stmt = null;
		CsvWriter csvWriter = new CsvWriter(stream);
		
		StringBuffer orderSQL = new StringBuffer();
		try {
			stmt = con.createStatement();
			stmt.execute(buff.toString());
			
			ResultSet rs = stmt.getResultSet();
			ResultSetMetaData metaData = rs.getMetaData();
			
			exportColumns(metaData, csvWriter);
			rs.close();
			
			DatabaseMetaData dmd = con.getMetaData();
			
			rs = dmd.getPrimaryKeys(null, null, tableName.toLowerCase());
			
			boolean first = true;
			while(rs.next()){
				if(first){
					orderSQL.append(" ORDER BY ");
					first = false;
				}else{
					orderSQL.append(", ");
				}
				String colName = rs.getString("COLUMN_NAME");
				orderSQL.append(colName);
			}
			rs.close();
		} catch (SQLException e) {
			throw new AlinousException(e, "Failed in backup");// i18n
		} catch (IOException e) {
			throw new AlinousException(e, "Failed in backup");// i18n
		}finally{
			if(stmt != null){
				try {
					stmt.close();
				} catch (SQLException e) {}
			}
		}
		
		try{
			maxCount = getMaxPages(tableName, (Connection)handle);
			int maxPages = maxCount / limit;
			if(maxCount % limit != 0){
				maxPages++;
			}
			
			for(int i = 0; i < maxPages; i++){
				int offset = i * limit;
				
				buff.delete(0, buff.length());
				buff.append("SELECT * FROM ");
				buff.append(tableName);
				buff.append(orderSQL.toString());
				buff.append(" LIMIT ");
				buff.append(Integer.toString(limit));
				buff.append(" OFFSET ");
				buff.append(offset);
				buff.append(";");
				AlinousDebug.debugOut(core, buff.toString());
				stmt = con.createStatement();
				stmt.execute(buff.toString());
				
				ResultSet rs = stmt.getResultSet();
				ResultSetMetaData metaData = rs.getMetaData();
				
				try{
					while(rs.next()){
						doExport(con, metaData, rs, csvWriter);
						
						csvWriter.endRecord();
					}
				}
				finally{
					if(rs != null){
						rs.close();
					}
					if(stmt != null){
						try {
							stmt.close();
						} catch (SQLException e) {}
					}
				}
			}
		} catch (SQLException e) {
			throw new AlinousException(e, "Failed in backup");// i18n
		} catch (IOException e) {
			throw new AlinousException(e, "Failed in backup");// i18n
		}
		
	}
	
	private int getMaxPages(String tableName, Connection con) throws SQLException
	{
		StringBuffer buff = new StringBuffer();
		buff.append("SELECT count(*) as CNT FROM ");
		buff.append(tableName);
		buff.append(";");
		
		
		Statement stmt = null;
		ResultSet rs = null;
		String value = null;
		try{
			stmt = con.createStatement();
			stmt.execute(buff.toString());
			
			rs = stmt.getResultSet();
			
			if(!rs.next()){
				return 0;
			}
		
			value = rs.getString(1);
		}
		finally{
			if(stmt != null){
				stmt.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		
		return Integer.parseInt(value);
	}
	
	private void exportColumns(ResultSetMetaData metaData, CsvWriter csvWriter) throws SQLException, IOException
	{
		int cnt = metaData.getColumnCount();
		for(int i = 0; i < cnt; i++){
			String value = metaData.getColumnName(i + 1);
			
			csvWriter.addField(value);
		}
		
		csvWriter.endRecord();		
	}
	
	private void doExport(Connection con, ResultSetMetaData metaData, ResultSet rs, CsvWriter csvWriter)
					throws SQLException, IOException
	{
		int cnt = metaData.getColumnCount();
		for(int i = 0; i < cnt; i++){
			String value = rs.getString(i + 1);
			csvWriter.addField(value);
		}
	}
}
