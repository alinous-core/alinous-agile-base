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

package org.alinous;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VirtualHostConfigCollection
{
	private Map<String, VirtualHostConfig> map = new HashMap<String, VirtualHostConfig>();
	
	public void addVirtualHostConfig(VirtualHostConfig config)
	{
		this.map.put(config.getHostName(), config);
	}
	
	public VirtualHostConfig getVirtualHostConfig(String host)
	{
		return this.map.get(host);
	}
	
	public Iterator<VirtualHostConfig> iterator()
	{
		ArrayList<VirtualHostConfig> list = new ArrayList<VirtualHostConfig>();
		
		Iterator<String> it = this.map.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			list.add(this.map.get(key));
		}
		
		return list.iterator();
	}
	
	public void removeHost(String serverName)
	{
		this.map.remove(serverName);
	}
	
	public void writeAsString(PrintWriter wr)
	{
		wr.write("	<hosts>\n");
		
		Iterator<String> it = this.map.keySet().iterator();
		while(it.hasNext()){
			String host = it.next();
			
			VirtualHostConfig config = this.map.get(host);
			config.writeAsString(wr);
		}
		
		wr.write("	</hosts>\n");
	}
}
