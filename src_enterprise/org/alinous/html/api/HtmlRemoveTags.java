/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2008 Tomohiro Iizuka
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
package org.alinous.html.api;

import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.html.HtmlTagRemover;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class HtmlRemoveTags extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "HTML.REMOVETAGS";
	
	public static final String HTML_STRING = "htmlString";
	
	public HtmlRemoveTags()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", HTML_STRING);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		ScriptDomVariable result;
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(HTML_STRING);
		IScriptVariable htmlString = newValRepo.getVariable(ipath, context);
		
		if(!(htmlString instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String html = ((ScriptDomVariable)htmlString).getValue();
		
		HtmlTagRemover remover = new HtmlTagRemover(html);
		
		result = new ScriptDomVariable("tmp");
		result.setValue(remover.removeTags());
		result.setValueType(IScriptVariable.TYPE_STRING);
		
		return result;
	}

	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Html.removeTags($htmlString)";
	}

	@Override
	public String descriptionString() {
		return "Remove html tag from $htmlString.";
	}

}