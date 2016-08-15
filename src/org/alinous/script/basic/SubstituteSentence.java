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
import org.alinous.script.basic.type.VariableDescriptor;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class SubstituteSentence implements IScriptSentence
{
	private VariableDescriptor operand;
	private IStatement statement;
	
	private int line;
	private int linePosition;
	private String filePath;
	
	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		context.getCore().reporttExecuted(this);
		
		try{
			IScriptVariable val = this.statement.executeStatement(context, valRepo);
			
			String prefix = this.operand.getPrefix();
			if(prefix.equals("$")){
				substituteVariable(val, context, valRepo);
			}else{
				substituteArray(val, context, valRepo);
			}
		}catch(ExecutionException e){
			e.addStackTrace(this.filePath, this.line);
			
			throw e;
		}
		
		return true;
	}
	
	private void substituteVariable(IScriptVariable val, PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		if(val == null){
			val = new ScriptDomVariable("null");
			((ScriptDomVariable)val).setValueType(IScriptVariable.TYPE_NULL);
		}
		else if(!(val instanceof ScriptDomVariable)){
			throw new ExecutionException("Cannot input Variable into ScriptDomVariable"); // i18n
		}
		
		IPathElement path = operand.getPath();
		
		// Calc Path at once
		String pathStr = path.getPathString(context, valRepo);
		path = PathElementFactory.buildPathElement(pathStr);
		
		try {
			valRepo.substitute(path, val, context);
		} catch(Throwable e){
			e.printStackTrace();
			throw new ExecutionException(e, "SubstituteSentence failed"); // i18n
		}
		
		// check variable input
		/*
		IScriptVariable val2 = valRepo.getVariable(path, context);
		if(val2 instanceof ScriptDomVariable){
			AlinousDebug.debugOut("After substituteVariable() : " + path.getPathString(context, valRepo)
					+ " : " + ((ScriptDomVariable)val2).getValue());
		}else if(val2 == null){
			AlinousDebug.debugOut("After substituteVariable() : " + path.getPathString(context, valRepo)
					+ " : " + null);
		}*/

		
	}
	
	private void substituteArray(IScriptVariable val, PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		if(!(val instanceof ScriptArray)){
			val = new ScriptArray("_tmp");
			//throw new ExecutionException("Cannot input Array into Variable : " + this.filePath + " line " + this.line); // i18n
		}
		
		IPathElement path = operand.getPath();
		String pathStr = path.getPathString(context, valRepo);
		path = PathElementFactory.buildPathElement(pathStr);

		try {
			valRepo.substitute(path, val, context);
			// valRepo.putValue(path, (IScriptVariable)val.clone(), context);
		} catch (CloneNotSupportedException e) {
			throw new ExecutionException(e, "Clone() failed"); // i18n
		}
	}
	
	public VariableDescriptor getOperand()
	{
		return operand;
	}

	public void setOperand(VariableDescriptor operand)
	{
		this.operand = operand;
	}

	public IStatement getStatement()
	{
		return statement;
	}

	public void setStatement(IStatement statement)
	{
		this.statement = statement;
		this.statement.setCallerSentence(this);
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

	public String getFilePath()
	{
		return this.filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
		
		this.statement.setFilePath(filePath);
		this.operand.setFilePath(filePath);
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element element = new Element(IStatement.TAG_STATEMENT);
		element.setAttribute(IStatement.ATTR_STATEMENT_CLASS, this.getClass().getName());
		parent.addContent(element);
		
		// statement
		this.statement.exportIntoJDomElement(element);

	}

	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element el = element.getChild(IStatement.TAG_STATEMENT);
		
		this.statement = StatementJDomFactory.createStatementFromDom(el);
		if(this.statement != null){
			this.statement.importFromJDomElement(el);
		}
		
	}

	public StepInCandidates getStepInCandidates()
	{
		StepInCandidates candidates = new StepInCandidates();
		
		this.statement.canStepInStatements(candidates);
		
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
		
		this.statement.checkStaticErrors(scContext, errorList);
		
		scContext.setCurrentExecutable(null);
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		this.statement.getFunctionCall(scContext, call, script);
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		LineCoverage lineCoverage = new LineCoverage(this.line, this.linePosition, this);
		coverage.addLineCoverage(lineCoverage);
		
	}
}
