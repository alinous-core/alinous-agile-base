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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.ISQLScriptObject;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;

public class SQLFunctionCallArguments implements ISQLScriptObject
{
	private List<ISQLStatement> arguments = new ArrayList<ISQLStatement>();
	
	public void addArgument(ISQLStatement argument)
	{
		this.arguments.add(argument);
	}


	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buffer = new StringBuffer();
		
		boolean first = true;
		Iterator<ISQLStatement> it = this.arguments.iterator();
		while(it.hasNext()){
			ISQLStatement stmt = it.next();
			
			if(first){
				first = false;
			}else{
				buffer.append(", ");
			}
			
			buffer.append(stmt.extract(context, providor, adjustWhere, adjustSet, helper));			
		}
		
		return buffer.toString();
	}
	
	public String extractPrecompile(PostContext context,
			VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buffer = new StringBuffer();
		
		boolean first = true;
		Iterator<ISQLStatement> it = this.arguments.iterator();
		while(it.hasNext()){
			ISQLStatement stmt = it.next();
			
			if(first){
				first = false;
			}else{
				buffer.append(", ");
			}
			
			buffer.append(stmt.extractPrecompile(context, providor, adjustWhere, adjustSet, helper));			
		}
		
		return buffer.toString();
	}
	

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		Iterator<ISQLStatement> it = this.arguments.iterator();
		while(it.hasNext()){
			ISQLStatement stmt = it.next();
			
			if(!stmt.isReady(context, providor, adjustWhere)){
				return false;
			}
		}
		
		return true;
	}



	
}
