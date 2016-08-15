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
import org.alinous.script.basic.condition.IScriptCondition;
import org.alinous.script.basic.condition.JDomConditionFactory;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class DummyConditionSentence implements IScriptSentence
{
	public static final String ATTR_LINE = "line";
	public static final String ATTR_LINE_POSITION = "linePosition";
	
	private int line;
	private int linePosition;
	private String filePath;
	
	private IScriptCondition condition;
	
	public DummyConditionSentence()
	{
		
	}
	
	public DummyConditionSentence(IScriptSentence sentence)
	{
		this.line = sentence.getLine();
		this.linePosition = sentence.getLinePosition();
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
	
	public StepInCandidates getStepInCandidates()
	{
		StepInCandidates candidates = new StepInCandidates();
		
		this.condition.canStepInStatements(candidates);
		
		return candidates;
	}
	
	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		context.getCore().reporttExecuted(this);
		return true;
	}
	
	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element sentenceElement = new Element(IExecutable.TAG_EXECUTABLE);
		
		sentenceElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		sentenceElement.setAttribute(ATTR_LINE, Integer.toString(this.line));
		sentenceElement.setAttribute(ATTR_LINE_POSITION, Integer.toString(this.linePosition));
		
		parent.addContent(sentenceElement);
		
		if(this.condition != null){
			this.condition.exportIntoJDomElement(sentenceElement);
		}
		
	}
	
	public void importFromJDomElement(Element element) throws AlinousException
	{
		// this sentence
		String strLine = element.getAttributeValue(ATTR_LINE);
		if(strLine != null){
			this.line = Integer.parseInt(strLine);
		}
		
		String strLinePosition = element.getAttributeValue(ATTR_LINE_POSITION);
		if(strLinePosition != null){
			this.linePosition = Integer.parseInt(strLinePosition);
		}
		
		// condition
		Element cond = element.getChild(IScriptCondition.TAG_CONDITION);
		this.condition = JDomConditionFactory.createConditionFromJDomElement(cond);
		
		this.condition.importFromJDomElement(cond);
	}
	
	public IScriptCondition getCondition()
	{
		return condition;
	}
	
	public void setCondition(IScriptCondition condition)
	{
		this.condition = condition;
		condition.setCallerSentence(this);
	}

	public IScriptVariable getReturnedVariable()
	{
		return null;
	}
	public void setCurrentDataSource(String dataSource)
	{
	}

	
	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
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
