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
package org.alinous.http.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

public class HttpUrlDecode extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "HTTP.URLDECODE";
	
	public static final String DECODE_STR = "decodeStr";
	public static final String ENCODE = "encode";
	
	public HttpUrlDecode()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", DECODE_STR);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", ENCODE);
		this.argmentsDeclare.addArgument(arg);
	}
	
	@Override
	public IScriptVariable executeFunction(PostContext context,
			VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(DECODE_STR);
		IScriptVariable decodeStringVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(ENCODE);
		IScriptVariable encodeVariable = newValRepo.getVariable(ipath, context);
		
		String decodeStr = ((ScriptDomVariable)decodeStringVariable).getValue();
		String encode = ((ScriptDomVariable)encodeVariable).getValue();
		
		ScriptDomVariable result = new ScriptDomVariable("result");
		result.setValueType(IScriptVariable.TYPE_NULL);
		
		String params[] = decodeStr.split("&");
		for(int i = 0; i < params.length; i++){
			String keyVal[] = params[i].split("=");
			
			if(keyVal.length == 2){
				ScriptDomVariable val = new ScriptDomVariable(keyVal[0]);
				val.setValueType(IScriptVariable.TYPE_STRING);
				try {
					val.setValue(URLDecoder.decode(keyVal[1], encode));
				} catch (UnsupportedEncodingException e) {
					context.getCore().getLogger().reportError(e);
					
					val.setValue("DECODE ERROR");
				}
				
				result.put(val);
			}else if(keyVal.length == 1){
				ScriptDomVariable val = new ScriptDomVariable(keyVal[0]);
				val.setValueType(IScriptVariable.TYPE_STRING);
				val.setValue("");
				
				result.put(val);
			}
			
		}

		return result;
	}

	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Http.urlDecode($decodeStr, $encode)";
	}

	@Override
	public String descriptionString() {
		return "";
	}

}
