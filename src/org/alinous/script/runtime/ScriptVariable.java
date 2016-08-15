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
package org.alinous.script.runtime;


public abstract class ScriptVariable implements IScriptVariable{
	protected String name;
	protected String value;
	protected String valueType = IScriptVariable.TYPE_NULL;
	
	public ScriptVariable()
	{
		
	}
	
	public ScriptVariable(String name)
	{
		this.name = name;
	}
	
	public void setValueType(String type)
	{
		this.valueType = type;
	}
	public String getValueType()
	{
		return this.valueType;
	}	
	
	public String getType()
	{
		return IScriptVariable.TYPE_NUMBER;
	}
	
	// Setter and Getter
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value = value;
		
		if(value == null){
			this.valueType = IScriptVariable.TYPE_NULL;
		}
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
	
	public void decInt()
	{
		int v = Integer.parseInt(this.value);
		this.value = Integer.toString(v - 1);
	}
	
	public void incInt()
	{
		int v = Integer.parseInt(this.value);
		this.value = Integer.toString(v + 1);
	}

}
