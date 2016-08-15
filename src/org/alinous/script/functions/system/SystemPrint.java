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

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class SystemPrint extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "SYSTEM.PRINTLN";
	
	public static String ARG_STRING = "argStr";
	
	
	public SystemPrint()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", ARG_STRING);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(ARG_STRING);
		IScriptVariable argStrVal = newValRepo.getVariable(ipath, context);
		
		if(!(argStrVal instanceof ScriptDomVariable)){
			throw new ExecutionException("Argument $str is wrong.");
		}
		
		ScriptDomVariable dom = (ScriptDomVariable)argStrVal;
		System.out.println(dom.getValue());
		System.out.flush();
		
		return null;
	}

	

	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "System.println($argStr)";
	}

	@Override
	public String descriptionString() {
		return "Output string '$argStr' into Standard output";
	}

}
