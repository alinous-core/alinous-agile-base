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

import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;

public interface IFunction
{
	public String getName();
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException;
	
	public ArgumentsDeclare getArguments();
	public void inputArguments(Stack<IStatement> stmtStack, PostContext context);
	
	//public IScriptVariable getResult();
	
	public void setCallerSentence(IScriptSentence callerSentence);
	public IScriptSentence getCallerSentence();
	
	public String codeAssistString();
	public String descriptionString();
}
