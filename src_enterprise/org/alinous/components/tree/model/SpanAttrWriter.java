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
package org.alinous.components.tree.model;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SpanAttrWriter
{
	private Map<String, String> events = new HashMap<String, String>();
	private Map<String, String> attrs = new HashMap<String, String>();
	
	public void addEventHandler(String ev, String method)
	{
		String handler = this.events.get(ev);
		
		if(handler == null){
			this.events.put(ev, method);
			
			return;
		}
		
		handler = handler + ";" + method;
		
		this.events.put(ev, handler);		
	}
	
	public void addAttribute(String key, String value)
	{
		this.attrs.put(key, value);
	}
	
	public void render(Writer wr) throws IOException
	{		
		Iterator<String> it = this.events.keySet().iterator();
		while(it.hasNext()){
			String evStr = it.next();
			String handler = this.events.get(evStr);
			
			wr.write(" ");
			wr.write(evStr);
			wr.write("=\"");
			wr.write(handler);
			wr.write("\"");
		}
		
		it = this.attrs.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			String val = this.attrs.get(key);
			
			wr.write(" ");
			wr.write(key);
			wr.write("=\"");
			wr.write(val);
			wr.write("\"");
		}
	}
}
