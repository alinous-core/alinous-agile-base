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

import org.alinous.AlinousConfig;
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

public class FunctionCallSentence implements IScriptSentence
{
	private FunctionCall functionCall;
	
	private int line;
	private int linePosition;
	private String filePath;

	private AlinousConfig config;
	

	
	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		try{
			this.functionCall.setFilePath(this.filePath);
			this.functionCall.executeStatement(context, valRepo);
			
			
		}catch(ExecutionException e){
			e.addStackTrace(this.filePath, this.line);
			
			throw e;
		}
		
		context.getCore().reporttExecuted(this);
		return true;
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element fancCallSentenceElement = new Element(IExecutable.TAG_EXECUTABLE);
		fancCallSentenceElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(fancCallSentenceElement);
		
		this.functionCall.exportIntoJDomElement(fancCallSentenceElement);	
	}

	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element el = element.getChild(IStatement.ATTR_STATEMENT_CLASS);
		
		if(el != null){
			this.functionCall = (FunctionCall) StatementJDomFactory.createStatementFromDom(el);
			this.functionCall.importFromJDomElement(el);
		}
		
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
		this.functionCall.setFilePath(filePath);
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

	public FunctionCall getFunctionCall()
	{
		return functionCall;
	}

	public void setFunctionCall(FunctionCall functionCall)
	{
		functionCall.setCallerSentence(this);
		this.functionCall = functionCall;
		
		functionCall.setFilePath(filePath);
	}

	public AlinousConfig getConfig()
	{
		return config;
	}

	public void setConfig(AlinousConfig config)
	{
		this.config = config;
	}
	
	public StepInCandidates getStepInCandidates()
	{
		StepInCandidates candidates = new StepInCandidates();
		
		this.functionCall.canStepInStatements(candidates);
		
		return candidates;
	}

	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		scContext.setCurrentExecutable(this);
		this.functionCall.checkStaticErrors(scContext, errorList);
		scContext.setCurrentExecutable(null);
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		this.functionCall.getFunctionCall(scContext, call, script);
	}
	
	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		LineCoverage lineCoverage = new LineCoverage(this.line, this.linePosition, this);
		coverage.addLineCoverage(lineCoverage);
		
	}
}
