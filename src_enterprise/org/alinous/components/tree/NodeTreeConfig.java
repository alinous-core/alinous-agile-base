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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NodeTreeConfig
{
	private List<NodeConfig> nodesList = new CopyOnWriteArrayList<NodeConfig>();
	
	public void addNode(NodeConfig node)
	{
		this.nodesList.add(node);
	}
	
	public Iterator<NodeConfig> iterator()
	{
		return this.nodesList.iterator();
	}
	
	public NodeConfig getNode(String id)
	{
		Iterator<NodeConfig> it = this.nodesList.iterator();
		while(it.hasNext()){
			NodeConfig co = it.next();
			
			if(co.getId().toUpperCase().equals(id.toUpperCase())){
				return co;
			}
		}
		
		return null;
	}
	
	public void remove(String nodeId)
	{
		NodeConfig delNode = getNode(nodeId);
		
		if(delNode != null){
			this.nodesList.remove(delNode);
		}
	}
	
	
	public void writeAsString(PrintWriter wr)
	{
		wr.write("	<treenodes>\n");
		
		Iterator<NodeConfig> it = this.nodesList.iterator();
		while(it.hasNext()){
			NodeConfig nodeConfig = it.next();
			
			nodeConfig.writeAsString(wr);
		}
		
		wr.write("	</treenodes>\n");
	}
}
