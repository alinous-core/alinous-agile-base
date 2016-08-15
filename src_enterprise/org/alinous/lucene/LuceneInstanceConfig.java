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
package org.alinous.lucene;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alinous.AlinousUtils;

public class LuceneInstanceConfig
{
	private String id;
	
	private String setupper;
	private String tmpPath;
	private String basePath;
	
	private String dataSrc;
	private String tableName;
	
	// param map
	private Map<String, String> map = new HashMap<String, String>();
	
	// if alinousHome is not null, return abstract path
	private String alinousHome;
	
	public void addProperty(String key, String value)
	{
		this.map.put(key, value);
	}
	
	public Map<String, String> getProperties()
	{
		return map;
	}
	
	public void clearProperties()
	{
		this.map.clear();
	}
	
	public String getId()
	{
		return id;
	}
	public void setId(String name)
	{
		this.id = name;
	}
	public String getBasePath()
	{
		if(this.alinousHome != null){
			return AlinousUtils.getAbsolutePath(this.alinousHome, basePath);
		}
		
		return basePath;
	}
	public String getRawBasePath()
	{
		return this.basePath;		
	}
	
	public void setBasePath(String basePath)
	{
		this.basePath = basePath;
	}
	public String getSetupper()
	{
		return setupper;
	}
	public void setSetupper(String setupper)
	{
		this.setupper = setupper;
	}
	public String getTmpPath()
	{
		if(this.alinousHome != null){
			return AlinousUtils.getAbsolutePath(this.alinousHome, basePath);
		}
		
		return tmpPath;
	}
	public String getRawTmpPath()
	{
		return this.tmpPath;
	}
	public void setTmpPath(String tmpPath)
	{
		this.tmpPath = tmpPath;
	}
	public String getDataSrc()
	{
		return dataSrc;
	}
	public void setDataSrc(String dataSrc)
	{
		this.dataSrc = dataSrc;
	}
	public String getTableName()
	{
		return tableName;
	}
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public void setAlinousHome(String alinousHome)
	{
		addProperty("ALINOUS_HOME", alinousHome);
		this.alinousHome = alinousHome;
	}
	
	public void writeAsString(PrintWriter wr)
	{
		wr.write("		<instance>\n");
		
		wr.write("			<id>" + this.id + "</id>\n");
		wr.write("			<setupper>" + this.setupper + "</setupper>\n");
		wr.write("			<tmppath>" + this.tmpPath + "</tmppath>\n");
		wr.write("			<basepath>" + this.basePath + "</basepath>\n");
		
		wr.write("			<datasrc>" + this.dataSrc + "</datasrc>\n");
		wr.write("			<table>" + this.tableName + "</table>\n");
		
		wr.write("			<params>\n");
		Iterator<String> it = this.map.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			String value = this.map.get(key);
			
			wr.write("				<" + key + ">");
			wr.write(value);
			wr.write("</" + key + ">\n");
		}
		wr.write("			</params>\n");
		
		wr.write("		</instance>\n");
	}
}
