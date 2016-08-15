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
package org.alinous.script.functions;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.alinous.AlinousCore;
import org.alinous.debug.DebugThread;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.AbstractScriptBlock;
import org.alinous.script.basic.Comment;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.jdom.Element;


public class FunctionDeclaration extends AbstractScriptBlock implements IFunction
{
	public FunctionDeclaration(String filePath) {
		super(filePath);
	}
	public FunctionDeclaration()
	{
		super();
	}
	public static final String BLOCK_NAME = "FuntionDeclaration";

	private String returnType;
	private String packageName;
	private String funcName;
	private ArgumentsDeclare arguments;
	
	private IScriptSentence callerSentence;
	
	private Comment funcComment;
	
	
	public String getName()
	{
		if(this.packageName != null){
			return this.packageName + "." + this.funcName;
		}
		return this.funcName;
	}
	
	
	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		// 
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		// DEBUG: inc executed StepIn count
		if(AlinousCore.debug(context)){
			DebugThread thread = context.getCore().getAlinousDebugManager().getCurrentThread();
			if(thread != null){
				thread.getTopStackFrame().incCandidate();
			}
		}
		
		
		// DEBUG: create stack frame
		if(AlinousCore.debug(context)){
			context.getCore().getAlinousDebugManager().createStackFrame(this, newValRepo, context);
		}
		
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence exec = it.next();
			
			boolean bl = executeSentence(exec, context, newValRepo);
			
			if(!bl){
				// DEBUG: destory stack frame
				if(AlinousCore.debug(context)){
					context.getCore().getAlinousDebugManager().destoryCurrentStackFrame();
				}
				
				// copy arguments changed
				// handleArgumentChanged(context, valRepo, newValRepo);
				
				return false;
			}
		}
		
		// DEBUG: destory stack frame
		if(AlinousCore.debug(context)){
			context.getCore().getAlinousDebugManager().destoryCurrentStackFrame();
		}
		
		// copy arguments changed
		// handleArgumentChanged(context, valRepo, newValRepo);
		
		return true;
	}

	
	private void handleArguments(PostContext context, VariableRepository valRepoOld,
								VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		// check number of arguments
		if(stmtStack == null && this.arguments != null){
			throw new ExecutionException("Number of the function is wrong.");
		}
		else if(stmtStack != null && !stmtStack.isEmpty() && this.arguments == null){
			throw new ExecutionException("Number of the function is wrong.");
		}
		else if(stmtStack == null && this.arguments == null){
			return; // means ok
		}
		else if(stmtStack.isEmpty() && this.arguments == null){
			return; // means ok
		}
		else if(stmtStack.size() != this.arguments.getSize()){
			throw new ExecutionException("Number of the function is wrong.");
		}
		
		// alias
		int size = this.arguments.getSize();
		for(int i = 0; i < size; i++){
			IStatement stmt = stmtStack.get(i);
			
			IScriptVariable val = stmt.executeStatement(context, valRepoOld);
			
			// DEBUG:
			if(AlinousCore.debug(context)){
				context.getCore().getAlinousDebugManager()
						.afterExecutedFunctionArgumentStatement(this.callerSentence, context);
			}
			
			ArgumentDeclare dec = this.arguments.get(i);
			
			// Type check, DOM or Array
			if(dec.getPrefix().equals("$")){
				if(!(val instanceof ScriptDomVariable)){
					throw new ExecutionException("Argument " + i + " must be DomVariable.");
				}				
			}
			else if(dec.getPrefix().equals("@")){
				if(val instanceof ScriptDomVariable 
						&& ((ScriptDomVariable)val).getValue() == null){
					val = new ScriptArray();
				}
				else if(!(val instanceof ScriptArray)){
					throw new ExecutionException("Argument " + i + " must be Array.");
				}
			}
			else{
				throw new ExecutionException("Argument " + i + " has no prefix.");
			}
			
			valRepo.putAlias(val, dec.getName());
		}
		
	}
	
	public void exportIntoJDomElement(Element parent)
	{
		
	}

	public void importFromJDomElement(Element threadElement)
	{
		
	}
	
	public ArgumentsDeclare getArguments()
	{
		return arguments;
	}

	public void setArguments(ArgumentsDeclare arguments)
	{
		this.arguments = arguments;
	}

	public String getPackageName()
	{
		return packageName;
	}

	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}

	public String getFuncName()
	{
		return funcName;
	}

	public void setFuncName(String funcName)
	{
		this.funcName = funcName;
	}

	public String getReturnType()
	{
		return returnType;
	}

	public void setReturnType(String returnType)
	{
		this.returnType = returnType;
	}

	public void inputArguments(Stack<IStatement> stmtStack, PostContext context)
	{
		context.pushNewFuncArgStack(stmtStack);
	}

	public void canStepInStatements(StepInCandidates candidates)
	{
		candidates.addCandidate(this);
	}

	public boolean canStepIn()
	{
		return true;
	}

	public StepInCandidates getStepInCandidates()
	{
		return null;
	}

	public void setCallerSentence(IScriptSentence callerSentence)
	{
		this.callerSentence = callerSentence;
		
	}

	public IScriptSentence getCallerSentence()
	{
		return this.getCallerSentence();
	}


	@Override
	public IScriptVariable executeFunction(PostContext context,
			VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		execute(context, valRepo);
		
		return context.getReturnedVariable(this);
	}


	@Override
	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}
	
	@Override
	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		super.checkStaticErrors(scContext, errorList);
	}
	@Override
	public String codeAssistString() {
		StringBuffer buff = new StringBuffer();
		if(this.packageName != null){
			buff.append(this.packageName);
			buff.append(".");
		}
		
		buff.append(this.funcName);
		buff.append("(");
		
		if(this.arguments != null){
			for(int i = 0; i < this.arguments.getSize(); i++){
				ArgumentDeclare argDec = this.arguments.get(i);
				
				if(i > 0){
					buff.append(", ");
				}
				
				buff.append(argDec.getPrefix());
				buff.append(argDec.getName());
			}
		}
		
		buff.append(")");
		
		return buff.toString();
	}
	
	@Override
	public String descriptionString() {
		if(this.funcComment == null){
			return null;
		}
		
		return this.funcComment.getComment();
	}
	
	public Comment getFuncComment() {
		return funcComment;
	}
	public void setFuncComment(Comment funcComment) {
		this.funcComment = funcComment;
	}

	
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		super.getFunctionCall(scContext, call, script);
	}
	
}
