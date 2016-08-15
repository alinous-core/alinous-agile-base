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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.expections.AlinousException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptObject;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.StatementJDomFactory;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class FuncArguments implements IScriptObject
{
	private int line;
	private int linePosition;
	private String filePath;
	
	public static final String TAG_ARGUMENTS = "ARGUMENTS";
	
	private List<IStatement> args = new LinkedList<IStatement>();
	
	public void addArgument(IStatement argument)
	{
		this.args.add(argument);
		argument.setFilePath(filePath);
	}
	
	public IStatement getStatement(int i)
	{
		return this.args.get(i);
	}
	
	public Iterator<IStatement> iterator()
	{
		return this.args.iterator();
	}
	
	public void setCallerSentence(IScriptSentence callerSentence)
	{
		Iterator<IStatement> it = this.args.iterator();
		while(it.hasNext()){
			IStatement stmt = it.next();
			stmt.setCallerSentence(callerSentence);
		}
	}
	
	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element element = new Element(TAG_ARGUMENTS);
		parent.addContent(element);
		
		Iterator<IStatement> it = this.args.iterator();
		while(it.hasNext()){
			IStatement stmt = it.next();
			
			stmt.exportIntoJDomElement(element);
		}
	}
	

	@SuppressWarnings("rawtypes")
	public void importFromJDomElement(Element element) throws AlinousException
	{
		List list = element.getChildren(IStatement.TAG_STATEMENT);
		Iterator it = list.iterator();
		while(it.hasNext()){
			Element el = (Element)it.next();
			
			IStatement stmt = StatementJDomFactory.createStatementFromDom(el);
			stmt.importFromJDomElement(el);
			
			this.args.add(stmt);
		}
	}
	
	public void canStepInStatements(StepInCandidates candidates)
	{
		Iterator<IStatement> it = this.args.iterator();
		while(it.hasNext()){
			IStatement stmt = it.next();
			
			stmt.canStepInStatements(candidates);
		}
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
		
		Iterator<IStatement> it = this.args.iterator();
		while(it.hasNext()){
			it.next().setFilePath(filePath);
		}
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		Iterator<IStatement> it = this.args.iterator();
		while(it.hasNext()){
			it.next().getFunctionCall(scContext, call, script);
		}
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}
	
	public int getSize()
	{
		return this.args.size();
	}
	
	
}
