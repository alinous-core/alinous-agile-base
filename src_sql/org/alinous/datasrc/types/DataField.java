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

import org.alinous.script.sql.ddl.CheckDefinition;


public class DataField
{
	public static final String TYPE_STRING = "String";
	public static final String TYPE_TEXT_STRING = "TEXT";
	public static final String TYPE_INTEGER = "Integer";
	public static final String TYPE_DOUBLE = "Double";
	public static final String TYPE_TIMESTAMP = "Timestamp";
	public static final String TYPE_BOOLEAN = "Boolean";
	public static final String TYPE_TIME = "Time";
	public static final String TYPE_DATE = "Date";
	
	
	public static final String TYPE_UNKNOWN = "unknown";
	
	private String name;
	private String type;
	private boolean primary;
	private boolean index;
	private int keyLength;
	private String defaultValue;
	private boolean defaultValueQuated;
	
	private boolean notnull;
	private boolean unique;
	private CheckDefinition check;
	
	public CheckDefinition getCheck()
	{
		return check;
	}

	public void setCheck(CheckDefinition check)
	{
		this.check = check;
	}

	public boolean isNotnull()
	{
		return notnull;
	}

	public void setNotnull(boolean notnull)
	{
		this.notnull = notnull;
	}

	public boolean isUnique()
	{
		return unique;
	}

	public void setUnique(boolean unique)
	{
		this.unique = unique;
	}

	public DataField()
	{
		
	}
	
	public DataField(String inName, String inType)
	{
		this.name = inName;
		this.type = inType;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isIndex() {
		return index;
	}

	public void setIndex(boolean index) {
		this.index = index;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public int getKeyLength()
	{
		return keyLength;
	}

	public void setKeyLength(int keyLength)
	{
		this.keyLength = keyLength;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue, boolean quated)
	{
		this.defaultValue = defaultValue;
		this.defaultValueQuated = quated;
	}
		
	public boolean isDefaultValueQuated() {
		return defaultValueQuated;
	}

	public String getDefaultString()
	{
		if(!this.defaultValueQuated){
			return " default " + this.defaultValue;
		}
		/*if(this.type == DataField.TYPE_INTEGER || this.type == DataField.TYPE_DOUBLE){
			return " default " + this.defaultValue;
		}*/
		return " default '" + this.defaultValue + "'";
	}

}
