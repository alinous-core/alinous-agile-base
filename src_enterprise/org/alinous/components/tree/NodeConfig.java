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
package org.alinous.components.tree;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NodeConfig
{
	private String id;
	private String datastore;
	
	private String openedImg = "/alinous-common/treenode/img/minus.gif";
	private String closedImg = "/alinous-common/treenode/img/plus.gif";
	
	private String openedClass = "nodeTreeOpened";

	
	private Map<String, DoctypeConfig> docTypes = new HashMap<String, DoctypeConfig>();
	private DoctypeConfig defaultDoctype;
	
	public void addDocType(DoctypeConfig docType)
	{
		this.docTypes.put(docType.getId(), docType);
	}
	
	public DoctypeConfig getDocType(String id)
	{
		return this.docTypes.get(id);
	}
	
	
	public String getTableName()
	{
		return "TREE_NODE_" + this.id.toUpperCase();
	}
	
	public String getTableModelSerialName()
	{
		return "TREE_NODE_" + this.id.toUpperCase() + "_SERIAL";
	}
	
	public String getDatastore()
	{
		return datastore;
	}
	public void setDatastore(String datastore)
	{
		this.datastore = datastore;
	}
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}

	public String getClosedImg()
	{
		return closedImg;
	}

	public void setClosedImg(String closedImg)
	{
		this.closedImg = closedImg;
	}

	public String getOpenedImg()
	{
		return openedImg;
	}

	public void setOpenedImg(String openedImg)
	{
		this.openedImg = openedImg;
	}

	public DoctypeConfig getDefaultDoctype()
	{
		return defaultDoctype;
	}

	public void setDefaultDoctype(DoctypeConfig defaultDoctype)
	{
		this.defaultDoctype = defaultDoctype;
	}
	
	public String getOpenedClass()
	{
		return openedClass;
	}

	public void setOpenedClass(String openedClass)
	{
		this.openedClass = openedClass;
	}

	public void writeAsString(PrintWriter wr)
	{
		wr.println("		<node>");
		
		// id
		wr.print("			<id>");
		wr.print(getId());
		wr.println("</id>");
		
		// dataSource
		wr.print("			<datastore>");
		wr.print(getDatastore());
		wr.println("</datastore>");
		
		// nodeTree opened
		if(this.openedClass != null){
			wr.print("			<openedClass>");
			wr.print(this.openedClass);
			wr.println("</openedClass>");
		}
		
		// doctypes
		Iterator<String> it = this.docTypes.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			DoctypeConfig dConfig = this.docTypes.get(key);
			dConfig.writeAsString(wr);
		}
		
		wr.println("		</node>");
	}
}
