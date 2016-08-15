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
package org.alinous.script.basic.type;

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
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;



public class VariableDescriptor implements IVariableDescription, IStatement
{
	private IPathElement path;
	private String prefix;
	
	private int line;
	private int linePosition;
	private String filePath;
	
	public VariableDescriptor()
	{
		
	}
	
	public VariableDescriptor(String prefix, IPathElement domPath)
	{
		this.prefix = prefix;
		this.path = domPath;
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

	public IPathElement getPath()
	{
		return path;
	}

	public String getPrefix()
	{
		return prefix;
	}
	
	public String toString(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		return this.prefix + this.path.getPathString(context, valRepo);
	}
	
	public IScriptVariable executeStatement(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		// VariableDescriptor
		// AlinousDebug.debugOut("VariableDescriptor#executeStatement() " + toString(context, valRepo));
		
		String pathStr = this.path.getPathString(context, valRepo);
		IPathElement pathElement =  PathElementFactory.buildPathElement(pathStr);
		IScriptVariable val = valRepo.getVariable(pathElement, context);
		
		//
		//AlinousDebug.debugOut("VariableDescriptor#executeStatement() val : " + val);
		
		if(val == null){
			val = new ScriptDomVariable("");
			((ScriptDomVariable)val).setValueType(IScriptVariable.TYPE_NULL);
		}
		
		return val;
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element element = new Element(IStatement.TAG_STATEMENT);
		element.setAttribute(IStatement.ATTR_STATEMENT_CLASS, this.getClass().getName());
		parent.addContent(element);
		
	}

	public void importFromJDomElement(Element element) throws AlinousException
	{
		
	}

	public void canStepInStatements(StepInCandidates candidates)
	{
		
	}

	public void setCallerSentence(IScriptSentence callerSentence)
	{
		
	}

	public void setCurrentDataSource(String dataSource)
	{
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
