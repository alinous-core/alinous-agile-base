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

import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.IExecutable;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.repository.AlinousModule;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.StatementJDomFactory;
import org.alinous.script.functions.FuncDeclarations;
import org.alinous.script.functions.FunctionDeclaration;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FuncArguments;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class IncludeSentence implements IScriptSentence
{
	private int line;
	private int linePosition;
	private String filePath;
	
	private FuncArguments args;

	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	public StepInCandidates getStepInCandidates()
	{
		StepInCandidates candidates = new StepInCandidates();
		
		this.args.canStepInStatements(candidates);
		
		return candidates;
	}

	public void setCurrentDataSource(String dataSource)
	{
		
	}

	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		context.getCore().reporttExecuted(this);
		
		this.args.setCallerSentence(this);
		
		IStatement pathStmt = this.args.getStatement(0);
		IScriptVariable filePathVariable = pathStmt.executeStatement(context, valRepo);
		
		if(!(filePathVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Include sentence is wrong", this.filePath, this.line); // i18n
		}
		
		String strPath = ((ScriptDomVariable)filePathVariable).getValue();
		
		// register alinous script module
		AlinousCore core = context.getCore();
		String moduleName = AlinousUtils.getModuleName(strPath);
		AlinousModule module = null;
		
		try {
			core.registerAlinousObject(context, moduleName);
			
			module = core.getModuleRepository().getModule(moduleName);
			
		} catch (AlinousException e) {
			throw new ExecutionException(e, "Include sentence is wrong", this.filePath, this.line); // i18n
		}
		
		if(module.getScript() == null){
			return true;
		}
		
		
		FuncDeclarations decs = module.getScript().getFuncDeclarations();
		
		Iterator<String> it = decs.iterateFuncNames();
		while(it.hasNext()){
			String funcName = it.next();
			
			FunctionDeclaration declare = context.getIncludeFuncDeclaration(funcName);
			if(declare != null){
				throw new ExecutionException("Include sentence is wrong. The function is already included", this.filePath, this.line); // i18n
			}
			
			declare = decs.findFunctionDeclare(funcName);
			
			context.putIncludeFuncDeclarations(declare);
		}
		
		
		return true;
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element includeElement = new Element(IExecutable.TAG_EXECUTABLE);
		includeElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(includeElement);
		
		// statement
		this.args.exportIntoJDomElement(includeElement);
	}

	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element el = element.getChild(FuncArguments.TAG_ARGUMENTS);
		
		this.args = (FuncArguments) StatementJDomFactory.createStatementFromDom(el);
		if(this.args != null){
			this.args.importFromJDomElement(el);
		}
	}	
	
	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
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
		
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}



}
