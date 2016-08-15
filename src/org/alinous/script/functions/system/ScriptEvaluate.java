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

import java.io.StringReader;
import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.parser.script.AlinousScriptParser;
import org.alinous.parser.script.ParseException;
import org.alinous.script.AlinousScript;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class ScriptEvaluate extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "SCRIPT.EVALUATE";
	public static String SCRIPT = "script";
	
	public ScriptEvaluate()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", SCRIPT);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(SCRIPT);
		IScriptVariable scriptVariable = newValRepo.getVariable(ipath, context);
		
		if(!(scriptVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String scriptString = ((ScriptDomVariable)scriptVariable).getValue();
		
		StringReader reader = new StringReader(scriptString);
		AlinousScriptParser parser = new AlinousScriptParser(reader);
		
		try {
			AlinousScript script = parser.parse();
			script.setFilePath(context.getRequestPath());
			script.setDataSourceManager(context.getCore().getDataSourceManager());
					
			script.execute(context, valRepo, false, false);
		} catch (ParseException e) {
			throw new ExecutionException(e, "The script you inputed is wrong."); // i18n
		}
		
		return null;
	}

	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Script.evaluate($script)";
	}

	@Override
	public String descriptionString() {
		return "Executes $script string as script. In the script, The variables at calling this function is available.";
	}

}
