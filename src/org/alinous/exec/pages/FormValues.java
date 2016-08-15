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

public class FormValues
{
	private Map<String, HashMap<String, IParamValue>> map = new HashMap<String, HashMap<String,IParamValue>>();
	
	public void putParam(String formName, String key, String value)
	{
		if(formName == null){
			formName = "";
		}
		
		HashMap<String, IParamValue> m = this.map.get(formName);
		
		if(m == null){
			m = new HashMap<String, IParamValue>();
			this.map.put(formName, m);
		}
		
		m.put(key, new StringParamValue(value));
	}
	
	public HashMap<String, IParamValue> getMap(String formName)
	{
		HashMap<String, IParamValue> retMap = this.map.get(formName);
		
		if(retMap == null){
			retMap = new HashMap<String, IParamValue>();
			this.map.put(formName, retMap);
		}
		
		return retMap;
	}
	
	public Iterator<String> getFormNameIterator()
	{
		return this.map.keySet().iterator();
	}
}
