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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.script.sql.ddl.CheckDefinition;
import org.alinous.script.sql.ddl.ForeignKey;
import org.alinous.script.sql.ddl.Unique;

public class DataTable {
	private List<DataField> fields = new ArrayList<DataField>();
	private String name;
	
	private List<String> primaryKeys = new ArrayList<String>();
	private List<Unique> unique = new ArrayList<Unique>();
	private List<CheckDefinition> check = new ArrayList<CheckDefinition>();
	private List<ForeignKey> foreignKey = new ArrayList<ForeignKey>();
	
	public DataTable()
	{
		
	}
	
	public DataTable(String tblName)
	{
		this.name = tblName;
	}
	
	public void addPrimaryKey(String fieldName)
	{
		this.primaryKeys.add(fieldName);
	}

	
	public int getNumFields()
	{
		return this.fields.size();
	}
	
	public List<String> getFields()
	{
		List<String> list = new LinkedList<String>();
		
		Iterator<DataField> it = this.fields.iterator();
		while(it.hasNext()){
			DataField fld = it.next();

			list.add(fld.getName());
		}
		
		return list;
	}
	
	public Iterator<DataField> iterator()
	{
		return this.fields.iterator();
	}
	
	public DataField getField(int i)
	{
		return this.fields.get(i);
	}
	
	public DataField getDataField(String fldName)
	{
		Iterator<DataField> it = this.fields.iterator();
		while(it.hasNext()){
			DataField fld = it.next();
			
			if(fld.getName().toUpperCase().equals(fldName.toUpperCase())){
				return fld;
			}
		}
		
		return null;
	}
	
	public void addField(DataField field)
	{
		this.fields.add(field);
		
	}
	
	public void addField(String fldName, String fldType)
	{
		DataField fld = new DataField(fldName, fldType);
		fld.setPrimary(false);
		
		addField(fld);
	}

	public void addField(String fldName, String fldType, boolean primary, int keyLength)
	{
		DataField fld = new DataField(fldName, fldType);
		fld.setPrimary(primary);
		fld.setKeyLength(keyLength);
		
		addField(fld);
	}
	
	public void addField(String fldName, String fldType, boolean primary)
	{
		DataField fld = new DataField(fldName, fldType);
		fld.setPrimary(primary);
		fld.setKeyLength(-1);
		
		addField(fld);
	}
	
	public void addField(String fldName, String fldType, boolean primary, boolean index)
	{
		DataField fld = new DataField(fldName, fldType);
		fld.setPrimary(primary);
		fld.setIndex(index);
		
		addField(fld);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setDefaultValue(String fieldName, String value, boolean quoted)
	{
		DataField fld = getDataField(fieldName);
		
		if(fld == null){
			return;
		}
		
		fld.setDefaultValue(value, quoted);
	}
	
	public List<DataField> getPrimaryKeys()
	{
		ArrayList<DataField> list = new ArrayList<DataField>();
		
		Iterator<String> it = this.primaryKeys.iterator();
		while(it.hasNext()){
			String fldName = it.next();
			
			DataField fld = getDataField(fldName);
			list.add(fld);
		}
		
		return list;
	}

	public List<Unique> getUnique()
	{
		return unique;
	}
	
	public void setUnique(List<Unique> unique)
	{
		Iterator<Unique> it = unique.iterator();
		while(it.hasNext()){
			Unique u = it.next();
			
			this.unique.add(u);
		}
	}
	
	public void addUnique(Unique unique)
	{
		this.unique.add(unique);
	}

	public List<CheckDefinition> getCheck()
	{
		return check;
	}

	public void setCheck(List<CheckDefinition> check)
	{
		this.check = check;
	}

	public List<ForeignKey> getForeignKey()
	{
		return foreignKey;
	}

	public void setForeignKey(List<ForeignKey> foreignKey)
	{
		this.foreignKey = foreignKey;
	}

	
	
}
