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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataSourceConfigCollection {
	private List<DataSourceConfig> dataSourceList = new CopyOnWriteArrayList<DataSourceConfig>();
	
	public void addDataSourceConfig(DataSourceConfig config)
	{
		this.dataSourceList.add(config);
	}
	
	public void removedDataSourceConfigById(String id)
	{
		Iterator<DataSourceConfig> it = this.dataSourceList.iterator();
		while(it.hasNext()){
			DataSourceConfig dc = it.next();
			
			if(id.equals(dc.getId())){
				this.dataSourceList.remove(dc);
				
				break;
			}
			
		}
	}
	
	public DataSourceConfig getDataSourceConfigById(String id)
	{
		Iterator<DataSourceConfig> it = this.dataSourceList.iterator();
		while(it.hasNext()){
			DataSourceConfig dc = it.next();
			
			if(id.equals(dc.getId())){
				return dc;
			}
			
		}
		
		return null;
	}
	
	public int getCount()
	{
		return this.dataSourceList.size();
	}
	
	public DataSourceConfig getDataSourceConfig(int i)
	{
		return this.dataSourceList.get(i);
	}
	
	public void writeAsString(PrintWriter wr)
	{
		Iterator<DataSourceConfig> it = this.dataSourceList.iterator();
		while(it.hasNext()){
			DataSourceConfig src = it.next();
			
			wr.print("	<datasources id=\"");
			wr.print(src.getId());
			wr.print("\"");
			wr.print(" class=\"");
			wr.print(src.getClazz());
			wr.println("\">");
			
			// connect
			wr.print("		<connect ");
			
			wr.print("maxclients=\"");
			wr.print(Integer.toString(src.getMaxclients()));
			wr.print("\"");
			wr.print(" ><![CDATA[");
			wr.print(src.getUri());
			wr.println("]]></connect>");
			
			// usr
			wr.print("		<user>");
			wr.print(src.getUser());
			wr.println("</user>");
			
			// pass
			wr.print("		<pass>");
			wr.print(src.getPass());
			wr.println("</pass>");
			
			wr.println("	</datasources>");
		}
		
	}
	
}
