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
package org.alinous.datasrc.types;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.alinous.script.runtime.IScriptVariable;

public class Record
{
	protected HashMap<String, String> values = new HashMap<String, String>();
	protected HashMap<String, String> valueTypes = new HashMap<String, String>();
	
	public Record()
	{
		
	}
	
	public void addFieldValue(String fieldName, String value, int sqlType)
	{
		if(this.values.containsKey(fieldName)){
			return;
		}
		
		this.values.put(fieldName, value);
		
		String valType = getAlinousDataType(sqlType);
		this.valueTypes.put(fieldName, valType);
	}
	
	public void addFieldValue(String fieldName, String value, String alinousType)
	{
		this.values.put(fieldName, value);
		this.valueTypes.put(fieldName, alinousType);
	}
	
	public Map<String, String> getMap()
	{
		return this.values;
	}
	
	public String getFieldValue(String fieldName)
	{
		return this.values.get(fieldName);
	}
	
	public String getFieldType(String fieldName)
	{
		return this.valueTypes.get(fieldName);
	}
	
	public void setFieldType(String fieldName, String type)
	{
		this.valueTypes.put(fieldName, type);
	}
	
	public static String getAlinousDataType(int sqlType)
	{
		String retType = null;
		
		switch(sqlType){
		case Types.INTEGER:
			retType = IScriptVariable.TYPE_NUMBER;
			break;
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.REAL:
			retType = IScriptVariable.TYPE_DOUBLE;
			break;
		case Types.BOOLEAN:
			retType = IScriptVariable.TYPE_BOOLEAN;
			break;
		case Types.NULL:
			retType = IScriptVariable.TYPE_NULL;
			break;
		default:
			retType = IScriptVariable.TYPE_STRING;
			break;
		}
		
		return retType;
	}
}
