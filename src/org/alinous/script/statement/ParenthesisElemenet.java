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
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class ParenthesisElemenet implements IStatement
{
	private int line;
	private int linePosition;
	private String filePath;
	
	private IStatement statement;

	private IScriptSentence callerSentence;
	
	public IStatement getStatement() {
		return statement;
	}

	public void setStatement(IStatement statement) {
		this.statement = statement;
		this.setCallerSentence(this.callerSentence);
		
		statement.setFilePath(filePath);
	}

	public IScriptVariable executeStatement(PostContext context, VariableRepository valRepo)
		throws ExecutionException, RedirectRequestException
	{
		return this.statement.executeStatement(context, valRepo);
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element element = new Element(IStatement.TAG_STATEMENT);
		element.setAttribute(IStatement.ATTR_STATEMENT_CLASS, this.getClass().getName());
		parent.addContent(element);
		
		statement.exportIntoJDomElement(element);
		
	}

	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element stmtElement = element.getChild(IStatement.TAG_STATEMENT);
		
		this.statement = StatementJDomFactory.createStatementFromDom(stmtElement);
		this.statement.importFromJDomElement(stmtElement);
		
	}

	public void canStepInStatements(StepInCandidates candidates)
	{
		this.statement.canStepInStatements(candidates);
		
	}

	public void setCallerSentence(IScriptSentence callerSentence)
	{
		this.callerSentence = callerSentence;
		
		this.statement.setCallerSentence(callerSentence);
		
	}

	public void setCurrentDataSource(String dataSource)
	{
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getLinePosition() {
		return linePosition;
	}

	public void setLinePosition(int linePosition) {
		this.linePosition = linePosition;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
		
		this.statement.setFilePath(filePath);
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		if(this.statement != null){
			this.statement.checkStaticErrors(scContext, errorList);
		}
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		if(this.statement != null){
			this.statement.getFunctionCall(scContext, call, script);
		}
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}
	
	
}
