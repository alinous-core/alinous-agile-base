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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LuceneConfig
{
	private List<LuceneInstanceConfig> configList = new ArrayList<LuceneInstanceConfig>();
	
	public void addConfig(LuceneInstanceConfig config)
	{
		this.configList.add(config);
	}
	
	public void removeConfig(String id)
	{
		LuceneInstanceConfig delConfig = null;
		Iterator<LuceneInstanceConfig> it = this.configList.iterator();
		while(it.hasNext()){
			LuceneInstanceConfig cfg = it.next();
			
			if(cfg.getId().equals(id)){
				delConfig = cfg;
				break;
			}
		}

		this.configList.remove(delConfig);
	}
	
	public Iterator<LuceneInstanceConfig> iterator()
	{
		return this.configList.iterator();
	}
	
	public LuceneInstanceConfig getInstanceConfig(String id)
	{
		Iterator<LuceneInstanceConfig> it = this.configList.iterator();
		while(it.hasNext()){
			LuceneInstanceConfig cfg = it.next();
			
			if(cfg.getId().equals(id)){
				return cfg;
			}
		}
		
		return null;
	}
	
	public void writeAsString(PrintWriter wr)
	{
		wr.write("	<lucene>\n");
		
		Iterator<LuceneInstanceConfig> it = this.configList.iterator();
		while(it.hasNext()){
			LuceneInstanceConfig config = it.next();
			
			config.writeAsString(wr);
		}
		
		wr.write("	</lucene>\n");
	}
}
