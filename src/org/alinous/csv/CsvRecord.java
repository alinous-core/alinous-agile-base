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
package org.alinous.csv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CsvRecord
{
	private List<String> list = new ArrayList<String>();
	
	public CsvRecord()
	{
	}
	
	public void addField(String fld)
	{
		list.add(fld);
	}
	
	public boolean isEmpty()
	{
		return list.isEmpty();
	}
	
	public String get(int index)
	{
		return this.list.get(index);
	}
	
	public String toString()
	{
		StringBuffer buff = new StringBuffer();
		
		boolean first = true;
		Iterator<String> it = this.list.iterator();
		while(it.hasNext()){
			String fld = it.next();
			
			if(first){
				first = false;
			}else{
				buff.append(",");
			}
			
			buff.append(fld);
		}
		
		return buff.toString();
	}
	
	public Iterator<String> iterator()
	{
		return this.list.iterator();
	}
}
