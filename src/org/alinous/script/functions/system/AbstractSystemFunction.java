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
package org.alinous.script.functions.system;

import java.util.Stack;

import org.alinous.AlinousCore;
import org.alinous.debug.ThreadTerminatedException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.ArgumentsDeclare;
import org.alinous.script.functions.IFunction;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;

public abstract class AbstractSystemFunction implements IFunction
{
	protected ArgumentsDeclare argmentsDeclare = new ArgumentsDeclare();
	private IScriptSentence callerSentence;

	public void inputArguments(Stack<IStatement> stmtStack, PostContext context)
	{
		context.pushNewFuncArgStack(stmtStack);
	}

	public ArgumentsDeclare getArguments()
	{
		return this.argmentsDeclare;
	}

	protected void handleArguments(PostContext context,
			VariableRepository valRepoOld, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException, ThreadTerminatedException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		// check number of arguments
		if(stmtStack == null && this.argmentsDeclare != null 
				&& this.argmentsDeclare.getSize() == 0){
			return;
		}
		else if (stmtStack == null && this.argmentsDeclare != null) {
			throw new ExecutionException("Number of the function is wrong.");// i18n
		} else if (stmtStack != null && this.argmentsDeclare == null) {
			throw new ExecutionException("Number of the function is wrong.");// i18n
		} else if (stmtStack == null && this.argmentsDeclare == null) {
			return;
		} else if (stmtStack.size() != this.argmentsDeclare.getSize()) {
			throw new ExecutionException("Number of the function is wrong.");// i18n
		}

		int size = this.argmentsDeclare.getSize();
		for (int i = 0; i < size; i++) {
			IStatement stmt = stmtStack.get(i);
			if(stmt instanceof FunctionCall){
				((FunctionCall)stmt).setCallerSentence(this.callerSentence);
			}
			
			IScriptVariable val = stmt.executeStatement(context, valRepoOld);
			
			// DEBUG:
			if (AlinousCore.debug(context)) {
				context.getCore().getAlinousDebugManager()
						.afterExecutedFunctionArgumentStatement(
								this.callerSentence, context);
			}

			ArgumentDeclare dec = this.argmentsDeclare.get(i);

			// Type check, DOM or Array
			if (dec.getPrefix().equals("$")) {
				if (!(val instanceof ScriptDomVariable)) {
					throw new ExecutionException("Argument " + i
							+ " must be DomVariable.");// i18n
				}
			} else if (dec.getPrefix().equals("@")) {
				if (!(val instanceof ScriptArray) && val != null) {
					//throw new ExecutionException("Argument " + i
					//		+ " must be Array.");// i18n
					val = new ScriptArray("tmp");
				}
				if(val == null){
					val = new ScriptArray("tmp");
				}
				
			} else {
				throw new ExecutionException("Argument " + i
						+ " has no prefix.");// i18n
			}

			// insert into repository
			valRepo.putAlias(val, dec.getName());
		}
	}
	
	public void setCallerSentence(IScriptSentence callerSentence)
	{
		this.callerSentence = callerSentence;
	}

	public IScriptSentence getCallerSentence()
	{
		return callerSentence;
	}

	public IScriptVariable executeFunction(PostContext context,
			VariableRepository valRepo) throws ExecutionException, RedirectRequestException {
		
		return null;
	}

	public String getName() {
		
		return null;
	}
	
}
