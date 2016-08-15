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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PostParams
{
	public static final String ALINOUS_TARGET_ATTR = "alns:target";
	
	private HashMap<String, IParamValue> params = new HashMap<String, IParamValue>();

	
	public void addParam(String key, String value)
	{
		this.params.put(key, new StringParamValue(value));
	}
	
	public IParamValue getParamValue(String key)
	{
		return this.params.get(key);
	}
	
	public void removeParam(String key)
	{
		this.params.remove(key);
	}
	
	public void setParamValue(String key, String value)
	{
		removeParam(key);
		this.params.put(key, new StringParamValue(value));
	}
	
	@SuppressWarnings("rawtypes")
	public void initParams(Map<String, IParamValue> params)
	{
		this.params.clear();
		
		Iterator it = params.keySet().iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			
			this.params.put(key, params.get(key));
		}
		
	}

	@SuppressWarnings("rawtypes")
	public void initParams(PostParams params)
	{
		this.params.clear();
		
		Iterator it = params.paramKeyIterator();
		while(it.hasNext()){
			String key = (String)it.next();
			
			this.params.put(key, params.get(key));
		}
		
	}
	
	public HashMap<String, IParamValue> getParamMap()
	{
		return this.params;
	}
	
	public Iterator<String> paramKeyIterator()
	{
		return this.params.keySet().iterator();
	}
	
	public IParamValue get(String key)
	{
		return this.params.get(key);
	}
	
}
