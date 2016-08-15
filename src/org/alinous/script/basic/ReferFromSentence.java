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
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.StatementJDomFactory;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FuncArguments;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class ReferFromSentence implements IScriptSentence
{
	private int line;
	private int linePosition;
	private String filePath;
	private FuncArguments args;
	
	public String getFilePath()
	{
		return this.filePath;
	}

	
	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	
	public StepInCandidates getStepInCandidates()
	{
		return null;
	}

	
	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	
	public boolean execute(PostContext context, VariableRepository valRepo)
			throws ExecutionException
	{
		context.getCore().reporttExecuted(this);
		return true;
	}

	
	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element referElement = new Element(IExecutable.TAG_EXECUTABLE);
		referElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(referElement);
		
		// statement
		this.args.exportIntoJDomElement(referElement);
	}

	
	public int getLine()
	{
		return this.line;
	}

	
	public void importFromJDomElement(Element threadElement)
			throws AlinousException
	{
		Element el = threadElement.getChild(FuncArguments.TAG_ARGUMENTS);
		
		this.args = (FuncArguments) StatementJDomFactory.createStatementFromDom(el);
		if(this.args != null){
			this.args.importFromJDomElement(el);
		}
	}

	
	public int getLinePosition()
	{
		return this.linePosition;
	}

	
	public void setLine(int line)
	{
		this.line = line;		
	}

	
	public void setLinePosition(int pos)
	{
		this.linePosition = pos;
	}
	
	public FuncArguments getArgs()
	{
		return args;
	}

	public void setArgs(FuncArguments args)
	{
		this.args = args;
	}


	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		
	}


	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		if(this.args != null){
			this.args.getFunctionCall(scContext, call, script);
		}
	}


	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}
}
