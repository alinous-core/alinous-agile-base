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
import org.alinous.test.Timestamp.DebugsetTimestamp;

public class SQLFunctionCallStatement implements ISQLStatement
{
	private String name;
	private SQLFunctionCallArguments arguments;
	
	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		String debugInterval = "";
		if(this.name.toLowerCase().equals("now") && DebugsetTimestamp.isTimeSet()){
			debugInterval = DebugsetTimestamp.getIntervalString();
		}
		
		if(this.arguments == null){
			return this.name + "()" + debugInterval;
		}
		
		return this.name + "(" + this.arguments.extract(context, providor, adjustWhere, adjustSet, helper) + ")"  + debugInterval;
	}
	
	@Override
	public String extractPrecompile(PostContext context,
			VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		String debugInterval = "";
		if(this.name.toLowerCase().equals("now") && DebugsetTimestamp.isTimeSet()){
			debugInterval = DebugsetTimestamp.getIntervalString();
		}
		
		if(this.arguments == null){
			return this.name + "()" + debugInterval;
		}
		
		return this.name + "(" + this.arguments.extractPrecompile(context, providor, adjustWhere, adjustSet, helper) + ")"  + debugInterval;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public SQLFunctionCallArguments getArguments()
	{
		return arguments;
	}

	public void setArguments(SQLFunctionCallArguments arguments)
	{
		this.arguments = arguments;
	}

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(this.arguments == null){
			return true;
		}
		return this.arguments.isReady(context, providor, adjustWhere);
	}



	
}
