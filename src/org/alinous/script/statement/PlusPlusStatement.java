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
package org.alinous.script.statement;

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
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class PlusPlusStatement implements IStatement
{
	private IStatement stmt;
	private String preOpe;
	private String postOpe;
	
	private IScriptSentence callerSentence;

	private int line;
	private int linePosition;
	private String filePath;
	
	public IStatement getStmt() {
		return stmt;
	}

	public void setStmt(IStatement stmt)
	{
		stmt.setCallerSentence(this.callerSentence);
		this.stmt = stmt;
		
		stmt.setFilePath(filePath);
	}

	public String getPostOpe()
	{
		return postOpe;
	}

	public void setPostOpe(String postOpe)
	{
		this.postOpe = postOpe;
	}

	public String getPreOpe()
	{
		return preOpe;
	}

	public void setPreOpe(String preOpe) {
		this.preOpe = preOpe;
	}

	public IScriptVariable executeStatement(PostContext context, VariableRepository valRepo)
											throws ExecutionException, RedirectRequestException
	{
		if(!(this.stmt instanceof VariableDescriptor)){
			throw new ExecutionException("Cannot use ++ operator to the statement but variable");
		}
		
		IScriptVariable val = this.stmt.executeStatement(context, valRepo);
		
		if(!(val instanceof ScriptDomVariable)){
			throw new ExecutionException("Cannot use ++ operator to Array");
		}
		
		ScriptDomVariable domVal = (ScriptDomVariable)val;
		if(domVal.getValueType() != IScriptVariable.TYPE_NUMBER){
			throw new ExecutionException("Cannot use ++ operator to String");
		}
		
		String strValue = domVal.getValue();
		int intValue = Integer.parseInt(strValue);
		
		// both case increment it
		if(this.preOpe != null &&this.preOpe.equals("++")){
			domVal.setValue(Integer.toString(intValue + 1));
		}else if(this.preOpe != null &&this.preOpe.equals("--")){
			domVal.setValue(Integer.toString(intValue - 1));
		}
		if(this.postOpe != null &&this.postOpe.equals("++")){
			domVal.setValue(Integer.toString(intValue + 1));
		}else if(this.postOpe != null &&this.postOpe.equals("--")){
			domVal.setValue(Integer.toString(intValue - 1));
		}
		
		
		if(this.preOpe != null){
			// return after added
			return domVal;
		}
		
		// return last value
		ScriptDomVariable defaultValue = new ScriptDomVariable("default");
		defaultValue.setValueType(IScriptVariable.TYPE_NUMBER);
		defaultValue.setValue(strValue);
		
		
		return defaultValue;
	}
	
	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element element = new Element(IStatement.TAG_STATEMENT);
		element.setAttribute(IStatement.ATTR_STATEMENT_CLASS, this.getClass().getName());
		parent.addContent(element);
		
		stmt.exportIntoJDomElement(element);
	}

	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element el = element.getChild(IStatement.TAG_STATEMENT);
		
		this.stmt = StatementJDomFactory.createStatementFromDom(el);
		this.stmt.importFromJDomElement(el);
		
	}

	public void canStepInStatements(StepInCandidates candidates)
	{
		this.stmt.canStepInStatements(candidates);
		
	}

	public void setCallerSentence(IScriptSentence callerSentence)
	{
		this.callerSentence = callerSentence;
		
		if(this.stmt != null){
			this.stmt.setCallerSentence(callerSentence);
		}
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

	public void setCurrentDataSource(String dataSource)
	{
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
		
		this.stmt.setFilePath(filePath);
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		if(this.stmt != null){
			this.stmt.checkStaticErrors(scContext, errorList);
		}
		
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		if(this.stmt != null){
			this.stmt.getFunctionCall(scContext, call, script);
		}
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}


	
}
