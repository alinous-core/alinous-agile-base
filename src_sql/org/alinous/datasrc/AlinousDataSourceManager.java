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

import java.util.Hashtable;
import java.util.Iterator;

import org.alinous.AlinousCore;
import org.alinous.datasrc.basic.ILogProvidor;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;


public class AlinousDataSourceManager {
	private Hashtable<String, IAlinousDataSource> dataSources = new Hashtable<String, IAlinousDataSource>();
	private ILogProvidor logger;
	private DataSourceConfigCollection config;
	
	public AlinousDataSourceManager(DataSourceConfigCollection config, ILogProvidor logger)
	{
		this.logger = logger;
		this.config = config;
	}

	public void init(AlinousCore core, String systemDataSrcId) {
		int cnt = config.getCount();
		for(int i = 0; i < cnt; i++){
			DataSourceConfig cfg = config.getDataSourceConfig(i);
			String clazz = cfg.getClazz();
			
			
			IAlinousDataSource ds = null;
			try {
				ds = (IAlinousDataSource)Class.forName(clazz).newInstance();
				ds.setLogger(this.logger, core);
				
				// result UPPER
				ds.setResultUpper(cfg.isResultUpper());
			} catch (InstantiationException e) {
				this.logger.reportError(e);
				continue;
			} catch (IllegalAccessException e) {
				this.logger.reportError(e);
				continue;
			} catch (ClassNotFoundException e) {
				this.logger.reportError(e);
				continue;
			}
			
			try {
				ds.setUri(cfg.getUri());
				ds.setUser(cfg.getUser());
				ds.setPass(cfg.getPass());
				ds.setMaxclients(cfg.getMaxclients());
				
				ds.init(core);
			} catch (DataSourceException e) {
				this.logger.reportError(e);
				continue;
			}
			
			String srcId = cfg.getId();
			this.dataSources.put(srcId, ds);
			
			// upper case
			if(srcId.toLowerCase().equals(systemDataSrcId.toLowerCase())){
				ds.setResultUpper(false);
				
				// debug
				//AlinousDebug.debugOut("SysDataSrc : " + srcId);
			}
		}
	}
	
	public DataSrcConnection connect(String dataSourceId, PostContext context) throws DataSourceException
	{
		IAlinousDataSource src = this.dataSources.get(dataSourceId);
		if(src == null){
			throw new DataSourceException("Datasource id : " + dataSourceId + " was not found."); // i18n
		}
		
		// not cached
		DataSrcConnection con = new DataSrcConnection(src, this.logger, context);
		con.connect();
		
		if(context != null){
			context.setDataSrc(dataSourceId);
		}
		
		return con;
	}

	public void dispose()
	{
		Iterator<String> it = this.dataSources.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			IAlinousDataSource src = this.dataSources.get(key);
			src.dispose();
		}
		
		this.dataSources.clear();
		
	}
	
	
	
}
