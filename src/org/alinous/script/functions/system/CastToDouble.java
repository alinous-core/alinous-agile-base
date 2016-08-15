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

public class CastToDouble extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "CAST.TODOUBLE";
	public static String ARRAY_ARG = "arg0";
	
	public CastToDouble()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", ARRAY_ARG);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(ARRAY_ARG);
		IScriptVariable val = newValRepo.getVariable(ipath, context);
		
		if(!(val instanceof ScriptDomVariable)){
			throw new ExecutionException(QUALIFIED_NAME + "() argument is wrong."); //i18n
		}
		
		ScriptDomVariable domValue = (ScriptDomVariable)val;
		
		try{
			Double.parseDouble(domValue.getValue());
		}catch(Throwable e){
			throw new ExecutionException(e, "Format error."); //i18n
		}
		
		domValue.setValueType(IScriptVariable.TYPE_DOUBLE);
		
		return domValue;
	}

	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Cast.toDouble($arg0)";
	}

	@Override
	public String descriptionString() {
		return "Cast the type of variable '$arg0' into Double.";
	}

}
