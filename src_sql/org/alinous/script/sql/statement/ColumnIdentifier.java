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
package org.alinous.script.sql.statement;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;

public class ColumnIdentifier implements ISQLStatement
{
	private String databaseName;
	private String tableName;
	private String columnName;
	//private VariableRepository valueProvidor;
	
	private String castType;
	
	
	public ColumnIdentifier(){}
	
	public ColumnIdentifier(String tableName, String columnName)
	{
		this.tableName = tableName;
		this.columnName = columnName;
	}
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getCastType() {
		return castType;
	}
	public void setCastType(String castType) {
		this.castType = castType;
	}
	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper)
	{
		String castStr = "";
		if(this.castType != null){
			castStr = "::" + this.castType;
		}
		
		if(this.databaseName != null && this.tableName != null){
			return this.databaseName + "." + this.tableName + "." + this.columnName + castStr;
		}
		
		if(this.tableName != null){
			return this.tableName + "." + this.columnName + castStr;
		}
		
		return this.columnName + castStr;
	}
	
	
	@Override
	public String extractPrecompile(PostContext context,
			VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException {
		return extract(context, providor, adjustWhere, adjustSet, helper);
	}
	
	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere)
	{
		return true;
	}


	
	
}
