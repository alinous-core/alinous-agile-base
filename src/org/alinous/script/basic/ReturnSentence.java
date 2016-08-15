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
package org.alinous.script.basic;

import java.util.List;

import org.alinous.debug.StepInCandidates;
import org.alinous.exec.IExecutable;
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
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class ReturnSentence implements IScriptSentence
{
	private int line;
	private int linePosition;
	private String filePath;
	
	private IStatement returnStatement;
	

	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		context.getCore().reporttExecuted(this);
		
		try{
			this.returnStatement.setCallerSentence(this);
			IScriptVariable returnedValue = this.returnStatement.executeStatement(context, valRepo);
			
			context.setReturnedVariable(this, returnedValue);
		}catch(ExecutionException e){
			e.addStackTrace(this.filePath, this.line);
			
			throw e;
		}
		
		return true;
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element returnElement = new Element(IExecutable.TAG_EXECUTABLE);
		returnElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(returnElement);
		
		// statement
		this.returnStatement.exportIntoJDomElement(returnElement);
	}

	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element el = element.getChild(IStatement.TAG_STATEMENT);
		
		this.returnStatement = StatementJDomFactory.createStatementFromDom(el);
		if(this.returnStatement != null){
			this.returnStatement.importFromJDomElement(el);
		}
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
		this.returnStatement.setFilePath(filePath);
	}

	public int getLine()
	{
		return line;
	}

	public void setLine(int line)
	{
		this.line = line;
	}

	public int getLinePosition()
	{
		return linePosition;
	}

	public void setLinePosition(int linePosition)
	{
		this.linePosition = linePosition;
	}

	public IScriptVariable getReturnedValue(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	public IStatement getReturnStatement()
	{
		return returnStatement;
	}

	public void setReturnStatement(IStatement returnStatement)
	{
		this.returnStatement = returnStatement;
	}

	public StepInCandidates getStepInCandidates()
	{
		StepInCandidates candidates = new StepInCandidates();
		
		this.returnStatement.canStepInStatements(candidates);
		
		return candidates;
	}

	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return getReturnedValue(context);
	}

	public void setCurrentDataSource(String dataSource)
	{
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		scContext.setCurrentExecutable(this);
		
		if(this.returnStatement != null){
			this.returnStatement.checkStaticErrors(scContext, errorList);
		}
		
		scContext.setCurrentExecutable(null);
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		if(this.returnStatement != null){
			this.returnStatement.getFunctionCall(scContext, call, script);
		}
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		LineCoverage lineCoverage = new LineCoverage(this.line, this.linePosition, this);
		coverage.addLineCoverage(lineCoverage);
		
	}
}
