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
package org.alinous.datasrc.types;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.IAlinousDataSource;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;

public class MetadataBag
{
	private HashMap<String, MetadataRecord> bagMap = new HashMap<String, MetadataRecord>();
	private IAlinousDataSource dataSource;
	
	public MetadataBag(IAlinousDataSource dataSource)
	{
		this.dataSource = dataSource;
	}
	
	public String getFieldType(String tableName, String columnName, PostContext context) throws DataSourceException
	{
		// : debug
		// AlinousDebug.debugOut("getFieldType : " + tableName + " . " + columnName);
		
		if(context == null){
			return null;
		}
		
		String cacheRet = hitCache(tableName, columnName);
		
		if(cacheRet != null){
			return cacheRet;
		}
		
		String curDataSrc = context.getDataSrc();
		if(curDataSrc == null){
			curDataSrc = context.getCore().getConfig().getSystemRepositoryConfig().getDefaultSrc();
		}
		
		
		//DataSrcConnection con = context.getUnit().getConnectionManager().connect(curDataSrc, context);
	//	DataSrcConnection con = context.getCore().getDataSourceManager().connect(curDataSrc, context);
		
		DataSrcConnection con = context.getCurrentDataSrcConnection();
		
		DataTable dataTable = null;
		try{
			dataTable = this.dataSource.getDataTable(con.getConnectionHandle(), tableName);
		}finally{
			// never close here
		}
		
		// : debug
		// AlinousDebug.debugOut("getFieldType : " + dataTable);
		
		// table does not exist
		if(dataTable == null){
			return null;
		}
		
		updateCache(tableName, dataTable);
		
		if(dataTable.getDataField(columnName) == null){
			return null;
		}
		
		return dataTable.getDataField(columnName).getType();
	}
	
	private String hitCache(String tableName, String columnName)
	{
		MetadataRecord rec = this.bagMap.get(tableName);
		if(rec == null){
			return null;
		}
		
		// check Timestamp
		long mill = System.currentTimeMillis();
		Timestamp tm = new Timestamp(mill);
		Calendar cal = Calendar.getInstance();
		cal.setTime(tm);
		
		cal.add(Calendar.SECOND, -10);
		tm =  new Timestamp(cal.getTimeInMillis());
		
		if(rec.getUpdateTime().after(tm)){
			return null;
		}
		
		// if ok use cached record
		return rec.getFieldType(columnName);
	}
	
	private void updateCache(String tableName, DataTable dataTable)
	{
		long mill = System.currentTimeMillis();
		Timestamp tm = new Timestamp(mill);
		
		MetadataRecord rec = new MetadataRecord(tableName, dataTable, tm);
		
		this.bagMap.put(tableName, rec);
	}
}
