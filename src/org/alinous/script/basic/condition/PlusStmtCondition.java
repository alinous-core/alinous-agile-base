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
package org.alinous.script.basic.condition;

import java.util.Iterator;
import java.util.List;

import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.script.statement.PlusStatement;
import org.alinous.script.statement.SubStatement;
import org.alinous.test.coverage.FileCoverage;



public class PlusStmtCondition extends AbstractStmtCondition
{
	public static final String OPE = "+";

	public IScriptVariable executeStatement(PostContext context,
			VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		PlusStatement stmt = new PlusStatement();
		
		boolean first = true;
		Iterator<IScriptCondition> it = this.conditions.iterator();
		while(it.hasNext()){
			IScriptCondition cnd = it.next();
			
			if(!(cnd instanceof IStatementCondition)){
				throw new ExecutionException("Cannot apply - operator to the condition.");
			}
			
			if(first){
				first = false;
				stmt.setFirst((IStatementCondition)cnd);
			}else{
				SubStatement subStmt = new SubStatement();
				subStmt.setOpe(OPE);
				subStmt.setTarget((IStatementCondition)cnd);
				
				stmt.addOperation(subStmt);
			}

		}
		
		
		return stmt.executeStatement(context, valRepo);
	}

	public void setCurrentDataSource(String dataSource)
	{
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		Iterator<IScriptCondition> it = this.conditions.iterator();
		while(it.hasNext()){
			IScriptCondition cnd = it.next();
			cnd.getFunctionCall(scContext, call, script);
		}
		
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}

}
