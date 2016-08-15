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
package org.alinous.script.sql.other;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.ISQLScriptObject;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.statement.SQLVariable;

public class TableIdentifier implements ISQLScriptObject
{
	private String databaseName;
	private String tableName;
	private String asName;
	
	private SQLVariable variable;
	
	//private VariableRepository valueProvidor;
	
	public TableIdentifier()
	{
		
	}
	
	public TableIdentifier(String tableName)
	{
		this.tableName = tableName;
	}
	
	public String getDatabaseName()
	{
		return databaseName;
	}
	
	public void setDatabaseName(String databaseName)
	{
		this.databaseName = databaseName;
	}
	
	public String getTableName()
	{
		return tableName;
	}
	
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}
	
	public String getAsName()
	{
		return asName;
	}

	public void setAsName(String asName)
	{
		this.asName = asName;
	}

	public String toString()
	{
		if(this.variable != null){
			try {
				return this.variable.extract(null, null, null, null, null);
			} catch (ExecutionException e) {
				return "";
			}
		}		
		
		if(this.databaseName == null){
			return this.tableName;
		}else{
			return this.databaseName + "." + this.tableName;
		}
	}
	
	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buffer = new StringBuffer();
		
		if(this.variable != null){
			String tmpVal = this.variable.extract(context, providor, adjustWhere, adjustSet, helper);
			buffer.append(tmpVal.substring(1, tmpVal.length() - 1));
		}
		else if(this.databaseName != null){
			buffer.append(this.databaseName + "." + this.tableName);
		}else{
			buffer.append(this.tableName);
		}
		
		if(this.asName != null){
			buffer.append(" AS ");
			buffer.append(this.asName);
		}
		
		return buffer.toString();
	}

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		return true;
	}

	public SQLVariable getVariable()
	{
		return variable;
	}

	public void setVariable(SQLVariable variable)
	{
		this.variable = variable;
	}
	
}
