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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.alinous.AlinousUtils;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.http.HttpDownloadConnector;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class HttpFetch extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "HTTP.FETCH";
	
	public static final String URL_STR = "urlStr";
	public static final String METHOD = "method";
	public static final String PARAMS = "postParams";
	public static final String OUT_FILENAME = "outFile";
	public static final String USER_AGENT = "userAgent";
	public static final String REFERER = "referer";
	
	public HttpFetch()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", URL_STR);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", METHOD);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", PARAMS);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", OUT_FILENAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", USER_AGENT);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", REFERER);
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(URL_STR);
		IScriptVariable urlStringVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(METHOD);
		IScriptVariable methodVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(PARAMS);
		IScriptVariable paramsVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(OUT_FILENAME);
		IScriptVariable outFileVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(USER_AGENT);
		IScriptVariable userAgentVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(REFERER);
		IScriptVariable refererVariable = newValRepo.getVariable(ipath, context);
		
		String urlString = ((ScriptDomVariable)urlStringVariable).getValue();
		String method = ((ScriptDomVariable)methodVariable).getValue();
		String outFile = ((ScriptDomVariable)outFileVariable).getValue();
		
		String userAgent = null;
		if(userAgentVariable != null){
			userAgent = ((ScriptDomVariable)userAgentVariable).getValue();
		}
		
		String referer = null;
		if(refererVariable != null){
			referer = ((ScriptDomVariable)refererVariable).getValue();
		}
		
		outFile = AlinousUtils.getAbsoluteNotOSPath(context.getCore().getHome(), outFile);
		
		Map<String, String> params = parseParams(paramsVariable);
		
		HttpDownloadConnector connector = new HttpDownloadConnector();
				
		ScriptDomVariable retVal = null;
		
		try {
			connector.download(urlString, method, params, outFile, userAgent, referer, context.getHttpAccessCookieManager());
			
			retVal = new ScriptDomVariable("result");
			retVal.setValueType(IScriptVariable.TYPE_BOOLEAN);
			retVal.setValue("true");
			
			parseResponceHeader(connector, retVal);
		} catch (URISyntaxException e) {
			context.getCore().getLogger().reportError(e);
		} catch (IOException e) {
			context.getCore().getLogger().reportError(e);
		}
		
		return retVal;
	}
	
	private void parseResponceHeader(HttpDownloadConnector connect, ScriptDomVariable domVariable)
	{
		Iterator<String> it = connect.getHeaders().keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			List<String> valList = connect.getHeaders().get(key);
			if(valList.isEmpty()){
				continue;
			}else if(valList.size() == 1){
				if(key == null){
					key = "RESPONSE";
				}
				
				ScriptDomVariable propVal = new ScriptDomVariable(key);
				propVal.setValueType(IScriptVariable.TYPE_STRING);
				propVal.setValue(valList.get(0));
				
				domVariable.put(propVal);
			}else{
				ScriptArray array = new ScriptArray(key);
				domVariable.put(array);
				
				Iterator<String> propIt = valList.iterator();
				while(propIt.hasNext()){
					String arrayVal = it.next();
					
					ScriptDomVariable propVal = new ScriptDomVariable("");
					propVal.setValueType(IScriptVariable.TYPE_STRING);
					propVal.setValue(arrayVal);
					
					array.add(propVal);
				}
			}
		}
	}
	
	private Map<String, String> parseParams(IScriptVariable val)
	{
		Map<String, String> params = new Hashtable<String, String>();
		
		if(!(val instanceof ScriptDomVariable)){
			return params;
		}
		
		ScriptDomVariable domVal = (ScriptDomVariable)val;
		
		Iterator<String> it = domVal.getPropertiesIterator();
		while(it.hasNext()){
			String prop = it.next();
			
			IScriptVariable propValue = domVal.get(prop);
			if(propValue == null || !(propValue instanceof ScriptDomVariable)){
				continue;
			}
			
			ScriptDomVariable propDomVal = (ScriptDomVariable)propValue;
			String strValue = propDomVal.getValue();
			
			if(strValue != null){
				params.put(prop, strValue);
			}
		}
		
		return params;
	}
	
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Http.fetch($urlStr, $method, $postParams, $outFile, $userAgent, $referer)";
	}

	@Override
	public String descriptionString() {
		return "Fetch data from url, $urlStr.\n" +
				"$urlStr can starts with 'http' or 'https'\n" +
				"\n" +
				"Post param should have http params." +
				"$postParams.param = \"val\";\n" +
				"";
	}
}
