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

import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.ISQLScriptObject;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.sql.statement.SQLStatement;
import org.alinous.script.sql.statement.SQLVariable;

public class SetPair implements ISQLScriptObject
{
	private ColumnIdentifier column;
	private ISQLStatement value;
	private AdjustSet adjustSet;
	
	public ColumnIdentifier getColumn() {
		return column;
	}
	public void setColumn(ColumnIdentifier column) {
		this.column = column;
	}
	public ISQLStatement getValue() {
		return value;
	}
	public void setValue(ISQLStatement value) {
		this.value = value;
	}
	
	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		// type valification		
		String columnName = column.extract(context, providor, adjustWhere, adjustSet, helper);
		
		String fieldType;
		try {
			fieldType = helper.getDataFieldType(columnName, context);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in extract IN clause."); // i18n
		}
		if(fieldType == null){
			return defaultExtract(context, providor, adjustWhere, adjustSet, helper);
		}
		
		TypeHelper helper2 = helper.newHelper(false);
		helper2.setFieldType(fieldType);
		
		if(this.value instanceof SQLStatement && ((SQLStatement)this.value).isNull(context, providor)){
			return columnName + " = NULL";
		}
		
		return columnName
			+ " = " + this.value.extract(context, providor, adjustWhere, adjustSet, helper2);
	}
	
	public String extractPrecompile(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		// type valification		
		String columnName = column.extract(context, providor, adjustWhere, adjustSet, helper);
		
		String fieldType;
		try {
			// type helper
			
			fieldType = helper.getDataFieldType(columnName, context);
			
			//
			// AlinousDebug.debugOut("********* Type helper : " + columnName + " -> " + fieldType);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in extract IN clause."); // i18n
		}
		if(fieldType == null){
			throw new ExecutionException("Could not detect type of " + columnName);
		}
		
		TypeHelper helper2 = helper.newHelper(false);
		helper2.setFieldType(fieldType);
		
		if(this.value instanceof SQLVariable){
			return columnName
					+ " = " + ((SQLVariable)this.value).extractPrecompile(context, providor, adjustWhere, adjustSet, helper2);
		}
		
		return columnName
			+ " = " + this.value.extract(context, providor, adjustWhere, adjustSet, helper2);
	}
	
	public String defaultExtract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		return column.extract(context, providor, adjustWhere, adjustSet, helper)
			+ " = " + this.value.extract(context, providor, adjustWhere, adjustSet, helper);
	}
	
	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(this.adjustSet != null && !this.adjustSet.adjust()){
			return true;
		}
		
		return this.value.isReady(context, providor, adjustWhere);
	}
	
	
}
