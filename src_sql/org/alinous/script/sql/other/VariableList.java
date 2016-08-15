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
import java.util.LinkedList;
import java.util.List;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.sql.statement.SQLNullConst;
import org.alinous.script.sql.statement.SQLVariable;
import org.alinous.script.sql.statement.SubQueryStatement;

public class VariableList
{
	private List<ISQLStatement> values = new LinkedList<ISQLStatement>();
	
	public void addValues(ISQLStatement value)
	{
		this.values.add(value);
	}
	
	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
	//	if(context.getPrecompile().isCompile()){
//			return extractPrecompile(context, providor, adjustWhere, adjustSet, helper);
	//	}
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("(");
		
		boolean first = true;
		Iterator<ISQLStatement> it = this.values.iterator();
		while(it.hasNext()){
			ISQLStatement stmt = it.next();
			
			if(stmt instanceof SQLVariable){
				SQLVariable sqVal = (SQLVariable)stmt;
				
				if(sqVal.isEmptyArray(context, providor)){
					continue;
				}
			}
			
			if(first){
				first = false;
			}else{
				buffer.append(" , ");
			}
			
			String fldType = helper.getCurrentListType();
			helper.incCount();
			
			TypeHelper helper2 = helper.newHelper(false);
			helper2.setFieldType(fldType);
			
			if(stmt instanceof SQLNullConst){
				buffer.append("null");
				continue;
			}
			
			// debug IN
			//AlinousDebug.debugOut("***************** IN stmt : " + stmt);
			
			String value = stmt.extract(context, providor, adjustWhere, adjustSet, helper2);
			
			buffer.append(value);
		}
		
		buffer.append(")");
		return buffer.toString();
	}
	/*
	public String extractPrecompile(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		Iterator<ISQLStatement> it = this.values.iterator();
		while(it.hasNext()){
			ISQLStatement stmt = it.next();
			
			
		}
		return null;
	}*/
	
	public boolean isNotArray()
	{
		if(values.size() == 1 && values.get(0) instanceof SubQueryStatement){
			return true;
		}
		
		return false;
	}
	
	public Iterator<ISQLStatement> iterator()
	{
		return this.values.iterator();
	}
	
	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(adjustWhere == null || !adjustWhere.adjust()){
			return true;
		}
		
		if(this.values == null || this.values.isEmpty()){
			return false;
		}
		
		Iterator<ISQLStatement> it = this.values.iterator();
		while(it.hasNext()){
			ISQLStatement stmt = it.next();
			
			if(!stmt.isReady(context, providor, adjustWhere)){
				return false;
			}
		}
		
		return true;
	}
}
