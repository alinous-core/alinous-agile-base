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

import java.util.ArrayList;
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

public class PlusStatement implements IStatement
{
	private int line;
	private int linePosition;
	private String filePath;
	
	public static final int OPERATION_INT = 1;
	public static final int OPERATION_DOUBLE = 2;
	public static final int OPERATION_STRING = 3;
	
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
		if(context == null){
			throw new ExecutionException("Context is null.");
		}
		
		IScriptVariable firstValueble = this.first.executeStatement(context, valRepo);
		if(!(firstValueble instanceof ScriptDomVariable)){
			throw new ExecutionException("Cannot multiple/divide array.");
		}
		
		List<OperationValuePair> valuesList = new ArrayList<OperationValuePair>();
		Iterator<IStatement> it = this.opeStatements.iterator();
		while(it.hasNext()){
			SubStatement subStmt = (SubStatement)it.next();
			
			// sub
			// AlinousDebug.debugOut("#### SubStatement subStmt : " + subStmt.getTarget());
			
			IScriptVariable val = subStmt.executeStatement(context, valRepo);
			if(!(val instanceof ScriptDomVariable)){
				throw new ExecutionException("Cannot multiple/divide array.");
			}
			
			valuesList.add(new OperationValuePair(subStmt.getOpe(), (ScriptDomVariable)val));
		}
		
		int operationType = getOperationType(valuesList, (ScriptDomVariable)firstValueble);
		
		IScriptVariable scVal = executePlus(valuesList, (ScriptDomVariable)firstValueble, operationType);
		
		return scVal;
	}
	
	private IScriptVariable executePlus(List<OperationValuePair> valuesList, ScriptDomVariable firstValueble,
							int operationType)
	{
		IScriptVariable scVal = null;
		switch(operationType){
		case OPERATION_INT:
			scVal = executePlusInt(valuesList, firstValueble, operationType);
			break;
		case OPERATION_DOUBLE:
			scVal = executePlusDouble(valuesList, firstValueble, operationType);
			break;
		default:
			scVal = executePlusString(valuesList, firstValueble, operationType);
			break;
		}
		
		return scVal;
	}
	
	private IScriptVariable executePlusString(List<OperationValuePair> valuesList, 
					ScriptDomVariable firstValueble, int operationType)
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(firstValueble.getValue());		
		
		Iterator<OperationValuePair> it = valuesList.iterator();
		while(it.hasNext()){
			OperationValuePair val = it.next();
			
			buffer.append(val.getValue().getValue());
		}
		
		ScriptDomVariable retVal = new ScriptDomVariable("");
		retVal.setValue(buffer.toString());
		retVal.setValueType(IScriptVariable.TYPE_STRING);
		
		return retVal;
	}
	
	
	private IScriptVariable executePlusInt(List<OperationValuePair> valuesList,
							ScriptDomVariable firstValueble, int operationType)
	{
		int retInt = Integer.parseInt(firstValueble.getValue());
		
		//AlinousDebug.debugOut("PLUS STATEMENT FIRST : " + retInt);
		
		Iterator<OperationValuePair> it = valuesList.iterator();
		while(it.hasNext()){
			OperationValuePair val = it.next();
			
			if(val.getOperation().equals("+")){
				retInt = retInt + Integer.parseInt(val.getValue().getValue());
			}
			else{
				retInt = retInt - Integer.parseInt(val.getValue().getValue());
			}
		}
		
		ScriptDomVariable retVal = new ScriptDomVariable("");
		retVal.setValue(Integer.toString(retInt));
		retVal.setValueType(IScriptVariable.TYPE_NUMBER);
		
		return retVal;
	}
	
	private IScriptVariable executePlusDouble(List<OperationValuePair> valuesList,
									ScriptDomVariable firstValueble, int operationType)
	{
		double retDouble = Double.parseDouble(firstValueble.getValue());
		
		Iterator<OperationValuePair> it = valuesList.iterator();
		while(it.hasNext()){
			OperationValuePair val = it.next();
			
			if(val.getOperation().equals("+")){
				retDouble = retDouble + Double.parseDouble(val.getValue().getValue());
			}
			else{
				retDouble = retDouble - Double.parseDouble(val.getValue().getValue());
			}
		}
		
		ScriptDomVariable retVal = new ScriptDomVariable("");
		retVal.setValue(Double.toString(retDouble));
		retVal.setValueType(IScriptVariable.TYPE_DOUBLE);
		
		return retVal;
	}
	
	private int getOperationType(List<OperationValuePair> valuesList, ScriptDomVariable firstValueble)
					throws AlinousNullPointerException
	{
		int ope = OPERATION_INT;
		
		if(firstValueble.getValueType().equals(IScriptVariable.TYPE_NULL)){
			throw new AlinousNullPointerException("Operand value is null.");
		}
		if(firstValueble.getValueType().equals(IScriptVariable.TYPE_NUMBER)){
			ope = OPERATION_INT;
		}
		else if(firstValueble.getValueType().equals(IScriptVariable.TYPE_DOUBLE)){
			ope = OPERATION_DOUBLE;
		}
		else{
			return OPERATION_STRING;
		}
		
		Iterator<OperationValuePair> it = valuesList.iterator();
		while(it.hasNext()){
			ScriptDomVariable val = it.next().getValue();
			
			if(val.getValueType().equals(IScriptVariable.TYPE_NULL)){
				throw new AlinousNullPointerException("Operand value is null.");
			}
			if(val.getValueType().equals(IScriptVariable.TYPE_NUMBER)){
				continue;	
			}
			else if(val.getValueType().equals(IScriptVariable.TYPE_DOUBLE)){
				ope = OPERATION_DOUBLE;
				continue;
			}
			else if(val.getValueType().equals(IScriptVariable.TYPE_BOOLEAN)){
				return OPERATION_STRING;
			}
			else if(val.getValueType().equals(IScriptVariable.TYPE_STRING)){
				return OPERATION_STRING;
			}
			
		}
		
		return ope;
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
		
		Iterator<IStatement> it = this.opeStatements.iterator();
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
			IStatement stmt = it.next();
			
			stmt.setCallerSentence(callerSentence);
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
			SubStatement subStmt = (SubStatement)it.next();
			subStmt.checkStaticErrors(scContext, errorList);
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
