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
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class DirectCondition implements IScriptCondition
{
	public static final String ATTR_OPE = "ope";
	
	public static String OPE_CMP_EQUALS = "==";
	public static String OPE_CMP_NOTEQUALS = "!=";
	public static String OPE_GT = ">";
	public static String OPE_GEQ = ">=";
	public static String OPE_LT = "<";
	public static String OPE_LEQ = "<=";
	
	private IScriptCondition left;
	private IScriptCondition right;
	private String ope;
	
	private IScriptSentence callerSentence;
	private int line;
	private int linePosition;
	private String filePath;
	
	public boolean evaluate(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		if(!(this.left instanceof IStatement)){
			throw new ExecutionException(this.ope + " cannot have condition operand");
		}
		if(!(this.right instanceof IStatement)){
			throw new ExecutionException(this.ope + " cannot have condition operand");
		}
		
		IScriptVariable leftValue = ((IStatement)this.left).executeStatement(context, valRepo);
		IScriptVariable rightValue = ((IStatement)this.right).executeStatement(context, valRepo);
		
		if(!(leftValue instanceof ScriptDomVariable)){
			throw new ExecutionException("Left value must be a variable");
		}
		if(!(rightValue instanceof ScriptDomVariable)){
			throw new ExecutionException("Right value must be a variable");
		}
		
		if(OPE_CMP_EQUALS.equals(this.ope)){
			return checkEquals((ScriptDomVariable)leftValue, (ScriptDomVariable)rightValue);
		}
		else if(OPE_CMP_NOTEQUALS.equals(this.ope)){
			return !checkEquals((ScriptDomVariable)leftValue, (ScriptDomVariable)rightValue);
		}
		else if(OPE_GT.equals(this.ope)){
			return checkGt((ScriptDomVariable)leftValue, (ScriptDomVariable)rightValue);
		}
		else if(OPE_GEQ.equals(this.ope)){
			return checkGtEq((ScriptDomVariable)leftValue, (ScriptDomVariable)rightValue);
		}
		else if(OPE_LT.equals(this.ope)){
			return checkLt((ScriptDomVariable)leftValue, (ScriptDomVariable)rightValue);
		}
		else if(OPE_LEQ.equals(this.ope)){
			return checkLtEq((ScriptDomVariable)leftValue, (ScriptDomVariable)rightValue);
		}
		
		return false;
	}

	private boolean checkLtEq(ScriptDomVariable leftValue, ScriptDomVariable rightValue)
		throws ExecutionException
	{
		if(leftValue.getValue() == null || rightValue.getValue() == null){
			throw new ExecutionException("Nullpointer Exception");
		}
		
		if(leftValue.getValueType().equals(IScriptVariable.TYPE_NUMBER) 
				&& rightValue.getValueType().equals(IScriptVariable.TYPE_NUMBER)){
			return Integer.parseInt(leftValue.getValue()) <= Integer.parseInt(rightValue.getValue());
		}
		if(leftValue.getValueType().equals(IScriptVariable.TYPE_DOUBLE) 
				|| rightValue.getValueType().equals(IScriptVariable.TYPE_DOUBLE)){
			return Double.parseDouble(leftValue.getValue()) <= Double.parseDouble(rightValue.getValue());
		}		
		
		int com = leftValue.getValue().compareTo(rightValue.getValue());
		
		return com <= 0;
	}
	
	private boolean checkLt(ScriptDomVariable leftValue, ScriptDomVariable rightValue)
		throws ExecutionException
	{
		if(leftValue.getValue() == null || rightValue.getValue() == null){
			throw new ExecutionException("Nullpointer Exception");
		}
		
		if(leftValue.getValueType().equals(IScriptVariable.TYPE_NUMBER)
				&& rightValue.getValueType().equals(IScriptVariable.TYPE_NUMBER)){
			return Integer.parseInt(leftValue.getValue()) < Integer.parseInt(rightValue.getValue());
		}
		if(leftValue.getValueType().equals(IScriptVariable.TYPE_DOUBLE) 
				|| rightValue.getValueType().equals(IScriptVariable.TYPE_DOUBLE)){
			return Double.parseDouble(leftValue.getValue()) < Double.parseDouble(rightValue.getValue());
		}
		
		int com = leftValue.getValue().compareTo(rightValue.getValue());
		
		return com < 0;
	}
	
	private boolean checkGtEq(ScriptDomVariable leftValue, ScriptDomVariable rightValue)
	throws ExecutionException
	{
		if(leftValue.getValue() == null || rightValue.getValue() == null){
			throw new ExecutionException("Nullpointer Exception");
		}
		
		if(leftValue.getValueType().equals(IScriptVariable.TYPE_NUMBER)
				&& rightValue.getValueType().equals(IScriptVariable.TYPE_NUMBER)){
			return Integer.parseInt(leftValue.getValue()) >= Integer.parseInt(rightValue.getValue());
		}
		if(leftValue.getValueType().equals(IScriptVariable.TYPE_DOUBLE) 
				|| rightValue.getValueType().equals(IScriptVariable.TYPE_DOUBLE)){
			return Double.parseDouble(leftValue.getValue()) >= Double.parseDouble(rightValue.getValue());
		}
		
		int com = leftValue.getValue().compareTo(rightValue.getValue());
		
		return com >= 0;
	}	
	
	private boolean checkGt(ScriptDomVariable leftValue, ScriptDomVariable rightValue)
		throws ExecutionException
	{
		if(leftValue.getValue() == null || rightValue.getValue() == null){
			throw new ExecutionException("Nullpointer Exception");
		}

		if(leftValue.getValueType().equals(IScriptVariable.TYPE_NUMBER) 
				&& rightValue.getValueType().equals(IScriptVariable.TYPE_NUMBER)){
			return Integer.parseInt(leftValue.getValue()) > Integer.parseInt(rightValue.getValue());
		}
		if(leftValue.getValueType().equals(IScriptVariable.TYPE_DOUBLE) 
				|| rightValue.getValueType().equals(IScriptVariable.TYPE_DOUBLE)){
			return Double.parseDouble(leftValue.getValue()) > Double.parseDouble(rightValue.getValue());
		}
		
		int com = leftValue.getValue().compareTo(rightValue.getValue());
		
		return com > 0;
	}
	
	private boolean checkEquals(ScriptDomVariable leftValue, ScriptDomVariable rightValue)
	{
		if(leftValue.getValue() == null){
			return rightValue.getValue() == null
				|| rightValue.getValueType().equals(IScriptVariable.TYPE_NULL);
		}
		else if(leftValue.getValueType().equals(IScriptVariable.TYPE_NULL)){
			return rightValue.getValue() == null
				|| rightValue.getValueType().equals(IScriptVariable.TYPE_NULL);
		}
		
		if(rightValue.getValue() == null){
			return leftValue.getValue() == null
					|| leftValue.getValueType().equals(IScriptVariable.TYPE_NULL);
		}
		else if(rightValue.getValueType().equals(IScriptVariable.TYPE_NULL)){
			return leftValue.getValue() == null
					|| leftValue.getValueType().equals(IScriptVariable.TYPE_NULL);
		}
		
		if(leftValue.getValueType().equals(IScriptVariable.TYPE_DOUBLE) 
				|| (rightValue.getValueType().equals(IScriptVariable.TYPE_DOUBLE) )){
			
			
			return isDouble(rightValue.getValue()) && Double.parseDouble(leftValue.getValue()) == Double.parseDouble(rightValue.getValue());
		}
		if(leftValue.getValueType().equals(IScriptVariable.TYPE_NUMBER) 
				&& rightValue.getValueType().equals(IScriptVariable.TYPE_NUMBER)){
			return isInteger(rightValue.getValue()) && Integer.parseInt(leftValue.getValue()) == Integer.parseInt(rightValue.getValue());
		}
		
		return leftValue.getValue().equals(rightValue.getValue());
	}
	
	private boolean isInteger(String str)
	{
		try{
			Integer.parseInt(str);
			return true;
		}
		catch(NumberFormatException e){
			
		}
		
		return false;
	}
	
	private boolean isDouble(String str)
	{
		try{
			Double.parseDouble(str);
			return true;
		}
		catch(NumberFormatException e){
			
		}
		
		return false;
	}
	
	
	public IScriptCondition getLeft()
	{
		return left;
	}
	
	public void setLeft(IScriptCondition left)
	{
		left.setCallerSentence(this.callerSentence);
		this.left = left;
		
		left.setFilePath(filePath);
	}
	
	public String getOpe() {
		return ope;
	}
	
	public void setOpe(String ope)
	{
		this.ope = ope;
	}
	
	public IScriptCondition getRight()
	{
		return right;
	}
	
	public void setRight(IScriptCondition right)
	{
		right.setCallerSentence(this.callerSentence);
		this.right = right;
		
		right.setFilePath(filePath);
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element element = new Element(IScriptCondition.TAG_CONDITION);
		element.setAttribute(IScriptCondition.ATTR_COND_CLASS, this.getClass().getName());
		
		element.setAttribute(ATTR_OPE, this.ope);
		
		parent.setContent(element);
		
		// handle statement
		this.left.exportIntoJDomElement(element);
		this.right.exportIntoJDomElement(element);
	}

	@SuppressWarnings("rawtypes")
	public void importFromJDomElement(Element element) throws AlinousException
	{
		this.ope = element.getAttributeValue(ATTR_OPE);
		
		List list = element.getChildren(IScriptCondition.TAG_CONDITION);
		
		Element lElement = (Element)list.get(0);
		this.left = JDomConditionFactory.createConditionFromJDomElement(lElement);
		this.left.importFromJDomElement(lElement);
		
		Element rElement = (Element)list.get(1);
		this.right = JDomConditionFactory.createConditionFromJDomElement(rElement);
		this.right.importFromJDomElement(rElement);
	}

	public void canStepInStatements(StepInCandidates candidates)
	{
		this.left.canStepInStatements(candidates);
		this.right.canStepInStatements(candidates);
	}

	public void setCallerSentence(IScriptSentence callerSentence)
	{
		this.callerSentence = callerSentence;
		
		this.right.setCallerSentence(callerSentence);
		this.left.setCallerSentence(callerSentence);
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
		
		this.left.setFilePath(filePath);
		this.right.setFilePath(filePath);
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		if(this.left != null){
			this.left.checkStaticErrors(scContext, errorList);
		}
		if(this.right != null){
			this.right.checkStaticErrors(scContext, errorList);
		}
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		if(this.left != null){
			this.left.getFunctionCall(scContext, call, script);
		}
		if(this.right != null){
			this.right.getFunctionCall(scContext, call, script);
		}
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}

}
