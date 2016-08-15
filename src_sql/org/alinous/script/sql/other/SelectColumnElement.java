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

import java.util.Iterator;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.sql.statement.SQLFunctionCallStatement;
import org.alinous.script.sql.statement.SQLVariable;

public class SelectColumnElement
{
	private ISQLStatement columnName;
	private Identifier asName;
	private String order;
	
	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException, RedirectRequestException
	{
		if(columnName instanceof SQLVariable){
			context.getPrecompile().setMarkVariable(true);
		}
		
		if(columnName instanceof SQLVariable &&
				((SQLVariable)columnName).getPrefix().equals("@")){
			return extractArray(context, providor, adjustWhere, adjustSet, helper);
		}
		
		StringBuffer buffer = new StringBuffer();
		
		String colNameStr = this.columnName.extract(context, providor, adjustWhere, adjustSet, helper);
		
		if(colNameStr.indexOf('\'') >= 0 && !(this.columnName instanceof SQLFunctionCallStatement)){
			int first = colNameStr.indexOf('\'');
			int last = colNameStr.indexOf('\'', first + 1);
			
			colNameStr = colNameStr.substring(first + 1, last);
		}
		buffer.append(colNameStr);

		
		if(this.asName != null){
			buffer.append(" AS ");
			buffer.append(this.asName.extract(context, providor, adjustWhere, adjustSet, helper));
		}
		
		if(this.order != null){
			buffer.append(" ");
			buffer.append(this.order);
		}
		
		return buffer.toString();
		
	}
	
	private String extractArray(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException, RedirectRequestException
	{
		context.getPrecompile().setHasArray(true);
		
		StringBuffer buffer = new StringBuffer();
		
		SQLVariable variable = (SQLVariable)this.columnName;
		
		IScriptVariable value = providor.getVariable(variable.getPathElement(), context);
		
		if(value instanceof ScriptArray){
			ScriptArray scArray = (ScriptArray)value;
			
			boolean first = true;
			Iterator<IScriptVariable> it = scArray.iterator();
			while(it.hasNext()){
				ScriptDomVariable domVal = (ScriptDomVariable) it.next();
				
				if(first){
					first = false;
				}else{
					buffer.append(",");
				}
				
				buffer.append(domVal.getValue());
				
				ScriptDomVariable orderDom = (ScriptDomVariable) domVal.get("ORDER");
				if(orderDom != null){
					buffer.append(" ");
					buffer.append(orderDom.getValue());
				}
			}
		}		
		
		return buffer.toString();
	}
	
	
	public Identifier getAsName()
	{
		return asName;
	}

	public void setAsName(Identifier asName)
	{
		this.asName = asName;
	}

	public ISQLStatement getColumnName()
	{
		return columnName;
	}

	public void setColumnName(ISQLStatement columnName)
	{
		this.columnName = columnName;
	}

	public void setColumnName(String columnName)
	{
		this.columnName = new Identifier(columnName);
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}
	
	
}
