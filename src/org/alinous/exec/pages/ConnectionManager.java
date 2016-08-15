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
package org.alinous.exec.pages;

import org.alinous.datasrc.AlinousDataSourceManager;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.lucene.extif.IDataStoreProvidor;

public class ConnectionManager implements IDataStoreProvidor
{
	private AlinousDataSourceManager dataSourceManager;
	//private Map<String, DataSrcConnection> pool = new ConcurrentHashMap<String, DataSrcConnection>();
	//private ConnectionManager parent;
	
	
	public ConnectionManager(AlinousDataSourceManager dataSourceManager)
	{
		this.dataSourceManager = dataSourceManager;
	}
	/*
	public ConnectionManager(AlinousDataSourceManager dataSourceManager, ConnectionManager parent)
	{
		this.dataSourceManager = dataSourceManager;
		//this.parent = parent;
	}

	public DataSrcConnection hasSelf(String dataSrc)
	{
		return this.pool.get(dataSrc);
	}
	
	public DataSrcConnection hasParent(String dataSrc)
	{
		if(this.parent == null){
			return null;
		}
		return this.parent.hasSelf(dataSrc);
	}
	*/
	public DataSrcConnection connect(String dataSrc, PostContext context) throws DataSourceException
	{
		if(context != null && context.getCurrentDataSrcConnection() != null){
			if(context.getDataSrc() != null && context.getDataSrc().equals(dataSrc) && context.getCurrentDataSrcConnection() != null){
				
				return context.getCurrentDataSrcConnection();
			}
		}
		
		DataSrcConnection conn = dataSourceManager.connect(dataSrc, context);
		
		// Assertion
		if(conn.isClosed()){
			throw new DataSourceException("connection is already closed");
		}
		

		if(context != null){
			if(context.getCurrentDataSrcConnection() != null && context.getCurrentDataSrcConnection() != conn){
				// listner will setCurrentDataSrcConnection(null)
				context.getCurrentDataSrcConnection().close();
			}
			
			// register connection and register closed listner in this
			context.setCurrentDataSrcConnection(conn);
			context.setDataSrc(dataSrc);

		}
		
		return conn;

	}
	
	public void clearCurrentDataSrc(PostContext context)
	{
		if(context.getCurrentDataSrcConnection() != null){
			context.getCurrentDataSrcConnection().close();
			context.setCurrentDataSrcConnection(null);
		}
		
		context.setDataSrc(null);
	}
	
	public void dispose()
	{
		/*
		Iterator<String> it = this.pool.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			DataSrcConnection conn = this.pool.get(key);
			
			conn.close();
			
			this.pool.remove(key);
		}
		*/
	}
	
}
