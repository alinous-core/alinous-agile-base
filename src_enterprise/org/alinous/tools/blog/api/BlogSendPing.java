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
package org.alinous.tools.blog.api;

import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.tools.blog.BlogPingManager;

public class BlogSendPing extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "BLOG.SENDPING";
	
	public static final String PING_URL = "pingUrl";
	public static final String WEBLOG_NAME = "weblogname";
	public static final String WEBLOG_URL = "weblogurl";
	
	public BlogSendPing()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", PING_URL);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", WEBLOG_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", WEBLOG_URL);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		ScriptDomVariable result;
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(PING_URL);
		IScriptVariable pingUrlVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(WEBLOG_NAME);
		IScriptVariable weblogNameVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(WEBLOG_URL);
		IScriptVariable weblogUrlVariable = newValRepo.getVariable(ipath, context);
		
		if(!(pingUrlVariable instanceof ScriptDomVariable) ||
				!(weblogNameVariable instanceof ScriptDomVariable) ||
				!(weblogUrlVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String pingUrl = ((ScriptDomVariable)pingUrlVariable).getValue();
		String weblogName = ((ScriptDomVariable)weblogNameVariable).getValue();
		String weblogUrl = ((ScriptDomVariable)weblogUrlVariable).getValue();
		
		BlogPingManager mgr = new BlogPingManager();
		mgr.setHttpUrl(pingUrl);
		
		String retStr = mgr.ping(weblogName, weblogUrl);
		
		result = new ScriptDomVariable("res");
		result.setValueType(IScriptVariable.TYPE_STRING);
		result.setValue(retStr);
		
		return result;
	}

	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return null;
	}

	@Override
	public String descriptionString() {
		return null;
	}

}
