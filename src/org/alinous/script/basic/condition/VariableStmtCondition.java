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

import java.util.List;

import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.StatementJDomFactory;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;



public class VariableStmtCondition extends AbstractStmtCondition
{
	private IStatement stmt;
	
	public IScriptVariable executeStatement(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		this.stmt.setCallerSentence(this.callerSentence);
		
		return this.stmt.executeStatement(context, valRepo);
	}

	public boolean evaluate(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		this.stmt.setCallerSentence(this.callerSentence);
		IScriptVariable val = null;
		try {
			val = this.stmt.executeStatement(context, valRepo);
		} catch (RedirectRequestException e) {
			e.printStackTrace();
		}
		
		if(val != null && val instanceof ScriptDomVariable){
			if(((ScriptDomVariable)val).getValueType() == IScriptVariable.TYPE_BOOLEAN &&
					((ScriptDomVariable)val).getValue() != null &&
					((ScriptDomVariable)val).getValue().toLowerCase().equals("true")){
				return true;
			}
		}
		
		return false;
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element element = new Element(IScriptCondition.TAG_CONDITION);
		element.setAttribute(IScriptCondition.ATTR_COND_CLASS, this.getClass().getName());
		parent.addContent(element);
		
		this.stmt.exportIntoJDomElement(element);
		
	}
	
	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element el = element.getChild(IStatement.TAG_STATEMENT);
		
		this.stmt = StatementJDomFactory.createStatementFromDom(el);
		this.stmt.importFromJDomElement(el);
	}
	
	public IStatement getStmt()
	{
		return stmt;
	}

	public void setStmt(IStatement stmt)
	{
		this.stmt = stmt;
	}

	public void setCurrentDataSource(String dataSource)
	{

	}

	@Override
	public void canStepInStatements(StepInCandidates candidates) {
		super.canStepInStatements(candidates);
		
		if(this.stmt != null){
			this.stmt.canStepInStatements(candidates);
		}
	}

	@Override
	public void setCallerSentence(IScriptSentence callerSentence) {
		super.setCallerSentence(callerSentence);
		
		if(this.stmt != null){
			this.stmt.setCallerSentence(callerSentence);
		}
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		if(this.stmt != null){
			this.stmt.checkStaticErrors(scContext, errorList);
		}
	}

	@Override
	public void setFilePath(String filePath) {
		super.setFilePath(filePath);
		
		this.stmt.setFilePath(filePath);
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		this.stmt.getFunctionCall(scContext, call, script);
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}

}
