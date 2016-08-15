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

import java.util.Iterator;
import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.VariableDescriptor;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class VariableListNames extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "VARIABLE.LISTNAMES";
	public static String LIST_ARG = "arg0";
	
	public VariableListNames()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", LIST_ARG);
		this.argmentsDeclare.addArgument(arg);
	}
	
	@Override
	public IScriptVariable executeFunction(PostContext context,
			VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != 1){
			throw new ExecutionException("Number of the function is wrong.");// i18n
		}
		
		IStatement stmt = stmtStack.get(0);
		
		if(!(stmt instanceof VariableDescriptor)){
			throw new ExecutionException("The argument must be a variable descriptor.");// i18n
		}
		
		VariableDescriptor variableDesc = (VariableDescriptor)stmt;
		
		IPathElement pathEl = variableDesc.getPath();
		
		IScriptVariable variable = valRepo.getVariable(pathEl, context);
		
		if(!(variable instanceof ScriptDomVariable)){
			return null;
		}
		
		ScriptDomVariable domVariable = (ScriptDomVariable)variable;
		
		// array variable to return
		ScriptArray retArray = new ScriptArray();
		
		Iterator<String> it = domVariable.getPropertiesIterator();
		while(it.hasNext()){
			String key = it.next();
			
			ScriptDomVariable val = new ScriptDomVariable("tmp");
			val.setValueType(IScriptVariable.TYPE_STRING);
			val.setValue(key);
			
			retArray.add(val);
		}
		
		return retArray;
	}

	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Variable.listNames($arg0)";
	}

	@Override
	public String descriptionString() {
		return "List up children of $arg0";
	}

}
