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
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptObject;
import org.alinous.script.IScriptSentence;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class NumericConst implements IStatement, IScriptObject
{
	private String number;
	private int line;
	private int linePosition;
	private String filePath;
	
	public NumericConst()
	{
		
	}
	
	public NumericConst(String number)
	{
		this.number = number;
	}
	
	public NumericConst(int number)
	{
		this.number = Integer.toString(number);
	}
	
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public IScriptVariable executeStatement(PostContext context, VariableRepository valRepo)
	{
		ScriptDomVariable val = new ScriptDomVariable("NumericConst");
		
		val.setValue(number);
		val.setValueType(IScriptVariable.TYPE_NUMBER);
		
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
