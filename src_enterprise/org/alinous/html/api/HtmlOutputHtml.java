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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.alinous.components.tree.seo.NodeTreeSeoManager;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.exec.pages.StringParamValue;
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

public class HtmlOutputHtml extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "HTML.OUTPUTHTML";
	
	public static final String TEMPLETE_PATH = "templetePath";
	public static final String OUT_PATH = "outPath";
	public static final String HTTP_PARAMS = "httpParams";
	public static final String ASYNC = "async";
	public static final String ENCODE = "encode";
	
	public HtmlOutputHtml()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", TEMPLETE_PATH);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", OUT_PATH);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", HTTP_PARAMS);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", ASYNC);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", ENCODE);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()
				&& stmtStack.size() != this.argmentsDeclare.getSize() - 1
				&& stmtStack.size() != this.argmentsDeclare.getSize() - 2){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(TEMPLETE_PATH);
		IScriptVariable htmlStringVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(OUT_PATH);
		IScriptVariable outPathVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(HTTP_PARAMS);
		IScriptVariable httpParamsVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(ASYNC);
		IScriptVariable asyncVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(ENCODE);
		IScriptVariable encodeVariable = newValRepo.getVariable(ipath, context);
		
		if(!(htmlStringVariable instanceof ScriptDomVariable) ||
				!(outPathVariable instanceof ScriptDomVariable) ||
				!(httpParamsVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		
		String tmpletePath = ((ScriptDomVariable)htmlStringVariable).getValue();
		String outPath = ((ScriptDomVariable)outPathVariable).getValue();
		String async = "false";
		if(asyncVariable != null ){
			async = ((ScriptDomVariable)asyncVariable).getValue();
		}
		String encode = "UTF-8";
		if(encodeVariable != null ){
			encode = ((ScriptDomVariable)encodeVariable).getValue();
		}
		
		// StaticHtmlWriter
		ScriptDomVariable paramDom = (ScriptDomVariable)httpParamsVariable;
		Map<String, IParamValue> paramMap = getParams(paramDom);
		paramMap.put(NodeTreeSeoManager.WRITE_STATIC, new StringParamValue("true"));
		
		HtmlOutputHtmlThread htmlOutput = new HtmlOutputHtmlThread(tmpletePath, outPath, encode,
							context.getSessionId(), context.getCore(), context, paramMap);
		if(async.toLowerCase().equals("true")){
			htmlOutput.start();
		}else{
			try{
				htmlOutput.doRun();
			}catch(Throwable e){
				e.printStackTrace();
				context.getCore().getLogger().reportError(e);
			}
		}
		/*
		StaticHtmlWriter htmlWriter = new StaticHtmlWriter(context.getCore());
		
		tmpletePath = AlinousUtils.getModuleName(tmpletePath);
		outPath = AlinousUtils.getAbsolutePath(context.getCore().getHome(), outPath);
		
		Writer writer = null;
		try {
			File file = new File(outPath);
			FileOutputStream stream = new FileOutputStream(file);
			writer = new OutputStreamWriter(stream, encode);
			
			htmlWriter.writeHtml(tmpletePath, writer, context, paramMap, context.getSessionId());
		} catch (IOException e) {
			throw new ExecutionException(e, "Failed in generating HTML.");
		} catch (AlinousException e) {
			throw new ExecutionException(e, "Failed in generating HTML.");
		} finally{
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		*/

		return null;
	}
	
	private Map<String, IParamValue> getParams(ScriptDomVariable paramDom)
	{
		HashMap<String, IParamValue> paramMap = new HashMap<String, IParamValue>();
		
		Iterator<String> it = paramDom.getPropertiesIterator();
		while(it.hasNext()){
			String paramName = it.next();
			
			IScriptVariable val = paramDom.get(paramName);
			if(val instanceof ScriptDomVariable){
				String strValue = ((ScriptDomVariable)val).getValue();
				
				StringParamValue strParamVal = new StringParamValue(strValue);
				paramMap.put(paramName, strParamVal);
			}
		}
		
		return paramMap;
	}
	
	/*
	private Map<String, IParamValue> handleParams(IScriptVariable httpParamsVariable)
	{
		Map<String, IParamValue> params = new HashMap<String, IParamValue>();
		
		if(httpParamsVariable instanceof ScriptDomVariable){
			
		}
		if(httpParamsVariable instanceof ScriptArray){
			
		}
		
		return params;
	}
	
	private void outputHtml(String templetePath, String outputFile)
	{
		
	}*/
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Html.outputHtml($templetePath, $outPath, $httpParams, $async, $encode)";
	}

	@Override
	public String descriptionString() {
			return "Acccess local server and store the result html.";
	}


}
