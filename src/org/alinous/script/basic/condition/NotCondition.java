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
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class NotCondition implements IScriptCondition
{
	private IScriptCondition condition;
	private IScriptSentence callerSentence;
	
	private int line;
	private int linePosition;
	private String filePath;
	
	public boolean evaluate(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		return !condition.evaluate(context, valRepo);
	}

	public IScriptCondition getCondition()
	{
		return condition;
	}

	public void setCondition(IScriptCondition condition)
	{
		condition.setCallerSentence(this.callerSentence);
		this.condition = condition;
		
		condition.setFilePath(filePath);
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element notElement = new Element(IScriptCondition.TAG_CONDITION);
		notElement.setAttribute(IScriptCondition.ATTR_COND_CLASS, this.getClass().getName());
		
		parent.addContent(notElement);
		
		condition.exportIntoJDomElement(notElement);
	}

	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element cond = element.getChild(IScriptCondition.TAG_CONDITION);
		this.condition = JDomConditionFactory.createConditionFromJDomElement(cond);
		
		this.condition.importFromJDomElement(cond);
	}

	public void canStepInStatements(StepInCandidates candidates)
	{
		this.condition.canStepInStatements(candidates);

	}

	public void setCallerSentence(IScriptSentence callerSentence)
	{
		this.callerSentence = callerSentence;
		this.condition.setCallerSentence(this.callerSentence);
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

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
		
		this.condition.setFilePath(filePath);
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		if(this.condition != null){
			this.condition.checkStaticErrors(scContext, errorList);
		}
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		this.condition.getFunctionCall(scContext, call, script);
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}
	
	
}
