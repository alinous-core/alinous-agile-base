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
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;

public class SQLStatement implements ISQLStatement
{
	private String distinct;
	private ISQLStatement firstStmt;
	private ISQLStatement subStatement;
	
	public SQLStatement()
	{
		this.firstStmt = null;
		this.subStatement = null;
	}
	
	// Getter and Setter
	public ISQLStatement getFirstStmt()
	{
		return firstStmt;
	}
	public void setFirstStmt(ISQLStatement firstStmt)
	{
		this.firstStmt = firstStmt;
	}
	public ISQLStatement getSubStatement()
	{
		return subStatement;
	}
	public void setSubStatement(ISQLStatement subStatement)
	{
		this.subStatement = subStatement;
	}

	public String getDistinct()
	{
		return distinct;
	}

	public void setDistinct(String distinct)
	{
		this.distinct = distinct;
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buffer = new StringBuffer();
		
		if(this.distinct != null){
			buffer.append(this.distinct);
			buffer.append(" ");
		}
		
		buffer.append(this.firstStmt.extract(context, providor, adjustWhere, adjustSet, helper));
		
		if(this.subStatement != null){
			buffer.append(" ");
			buffer.append(this.subStatement.extract(context, providor, adjustWhere, adjustSet, helper));
		}
		
		return  buffer.toString();
	}
	
	@Override
	public String extractPrecompile(PostContext context,
			VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buffer = new StringBuffer();
		
		if(this.distinct != null){
			buffer.append(this.distinct);
			buffer.append(" ");
		}
		
		buffer.append(this.firstStmt.extractPrecompile(context, providor, adjustWhere, adjustSet, helper));
		
		if(this.subStatement != null){
			buffer.append(" ");
			buffer.append(this.subStatement.extractPrecompile(context, providor, adjustWhere, adjustSet, helper));
		}
		
		return  buffer.toString();
	}

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(this.firstStmt != null && this.firstStmt.isReady(context, providor, adjustWhere)){
			return true;
		}
		
		return false;
	}
	
	public boolean isNull(PostContext context, VariableRepository providor) throws ExecutionException
	{
		if(this.firstStmt != null && this.firstStmt instanceof SQLVariable){
			SQLVariable sqlVal = (SQLVariable)this.firstStmt;
			
			IScriptVariable scVal = sqlVal.getScriptVariable(context, providor);
			if(scVal == null){
				return true;
			}
			if(scVal instanceof ScriptDomVariable){
				ScriptDomVariable domVal = (ScriptDomVariable)scVal;
				
				if(domVal.getValue() == null || domVal.getValueType().equals(IScriptVariable.TYPE_NULL)){
					return true;
				}
			}
		}
		
		return false;
	}

}
