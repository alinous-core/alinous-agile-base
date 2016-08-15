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
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.AlinousNullPointerException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.StatementJDomFactory;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class MultipleStatement implements IStatement
{
	private int line;
	private int linePosition;
	private String filePath;
	
	private IStatement first;
	private List<IStatement> opeStatements = new LinkedList<IStatement>();
	
	private IScriptSentence callerSentence;
	
	public void addOperation(IStatement ope)
	{
		ope.setCallerSentence(this.callerSentence);
		this.opeStatements.add(ope);
		
		ope.setFilePath(filePath);
	}
	
	public IStatement getFirst()
	{
		return first;
	}
	
	public void setFirst(IStatement first)
	{
		first.setCallerSentence(this.callerSentence);
		this.first = first;
		
		first.setFilePath(filePath);
	}

	public IScriptVariable executeStatement(PostContext context, VariableRepository valRepo)
					throws ExecutionException, RedirectRequestException
	{
		if(isDouble(context, valRepo)){
			return executeDouble(context, valRepo);
		}
				
		int intValue = 0;
		
		IScriptVariable val = first.executeStatement(context, valRepo);
		checkValue(val);
		
		intValue = Integer.parseInt(((ScriptDomVariable)val).getValue());
		
		Iterator<IStatement> it = this.opeStatements.iterator();
		while(it.hasNext()){
			SubStatement subStmt = (SubStatement)it.next();
			
			val = subStmt.getTarget().executeStatement(context, valRepo);
			checkValue(val);
			
			int tmpVal = Integer.parseInt(((ScriptDomVariable)val).getValue());
			if(subStmt.getOpe().equals("*")){
				intValue = intValue * tmpVal;
			}else{
				intValue = intValue / tmpVal;
			}
		}
		
		ScriptDomVariable retValue = new ScriptDomVariable("");
		retValue.setValue(Integer.toString(intValue));
		retValue.setValueType(ScriptDomVariable.TYPE_NUMBER);
		
		return retValue;
	}
	
	private IScriptVariable executeDouble(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		double dblValue = 0;
		
		IScriptVariable val = first.executeStatement(context, valRepo);
		dblValue = Double.parseDouble(((ScriptDomVariable)val).getValue());
		
		Iterator<IStatement> it = this.opeStatements.iterator();
		while(it.hasNext()){
			SubStatement subStmt = (SubStatement)it.next();
			
			val = subStmt.getTarget().executeStatement(context, valRepo);
			double tmpVal = Double.parseDouble(((ScriptDomVariable)val).getValue());
			if(subStmt.getOpe().equals("*")){
				dblValue = dblValue * tmpVal;
			}else{
				dblValue = dblValue / tmpVal;
			}
			
		}
		
		ScriptDomVariable retValue = new ScriptDomVariable("");
		retValue.setValue(Double.toString(dblValue));
		retValue.setValueType(ScriptDomVariable.TYPE_DOUBLE);
		
		return retValue;
	}
	
	private boolean isDouble(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		IScriptVariable val = first.executeStatement(context, valRepo);
		checkValue(val);
		
		String type = ((ScriptDomVariable)val).getValueType();
		if(type.equals(IScriptVariable.TYPE_DOUBLE)){
			return true;
		}
		
		Iterator<IStatement> it = this.opeStatements.iterator();
		while(it.hasNext()){
			SubStatement subStmt = (SubStatement)it.next();
			
			val = subStmt.getTarget().executeStatement(context, valRepo);
			checkValue(val);
			
			type = ((ScriptDomVariable)val).getValueType();
			if(type.equals(IScriptVariable.TYPE_DOUBLE)){
				return true;
			}
		}
		
		return false;
	}
	
	private void checkValue(IScriptVariable val) throws ExecutionException
	{
		if(!(val instanceof ScriptDomVariable)){
			throw new ExecutionException("Cannot multiple/divide array."); // i18n
		}

		if(((ScriptDomVariable)val).getValueType().equals(IScriptVariable.TYPE_NULL) ){
			throw new AlinousNullPointerException("Null Pointer Exception"); // i18n
		}
		
		String type = ((ScriptDomVariable)val).getValueType();
		
		if(!type.equals(IScriptVariable.TYPE_NUMBER) &&  !type.equals(IScriptVariable.TYPE_DOUBLE)){
			throw new ExecutionException("Cannot multiple/divide String nor boolean."); // i18n
		}
	}
	
	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element element = new Element(IStatement.TAG_STATEMENT);
		element.setAttribute(IStatement.ATTR_STATEMENT_CLASS, this.getClass().getName());
		parent.addContent(element);
		
		// first
		this.first.exportIntoJDomElement(element);
		
		// others
		Iterator<IStatement> it = this.opeStatements.iterator();
		while(it.hasNext()){
			IStatement stmt = it.next();
			
			stmt.exportIntoJDomElement(element);
		}
		
	}

	@SuppressWarnings("rawtypes")
	public void importFromJDomElement(Element element) throws AlinousException
	{
		boolean first = true;
		Iterator it = element.getChildren(IStatement.TAG_STATEMENT).iterator();
		while(it.hasNext()){
			Element el = (Element)it.next();
			
			IStatement stmt = StatementJDomFactory.createStatementFromDom(el);
			stmt.importFromJDomElement(el);
			
			if(first){
				first = false;
				this.first = stmt;
			}else{
				this.opeStatements.add(stmt);
			}
			
		}
		
	}

	public void canStepInStatements(StepInCandidates candidates)
	{
		this.first.canStepInStatements(candidates);
		
		Iterator<IStatement> it = this.opeStatements.iterator();// i18n
		while(it.hasNext()){
			IStatement stmt = it.next();
			
			stmt.canStepInStatements(candidates);
		}
	}

	public void setCallerSentence(IScriptSentence callerSentence)
	{
		this.callerSentence = callerSentence;
		
		this.first.setCallerSentence(callerSentence);
		Iterator<IStatement> it = this.opeStatements.iterator();
		while(it.hasNext()){
			it.next().setCallerSentence(callerSentence);
		}
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
		
		this.first.setFilePath(filePath);
		Iterator<IStatement> it = this.opeStatements.iterator();
		while(it.hasNext()){
			it.next().setFilePath(filePath);
		}
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		if(this.first != null){
			this.first.checkStaticErrors(scContext, errorList);
		}
		Iterator<IStatement> it = this.opeStatements.iterator();
		while(it.hasNext()){
			it.next().checkStaticErrors(scContext, errorList);
		}
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		if(this.first != null){
			this.first.getFunctionCall(scContext, call, script);
		}
		Iterator<IStatement> it = this.opeStatements.iterator();
		while(it.hasNext()){
			it.next().getFunctionCall(scContext, call, script);
		}
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}
	
}
