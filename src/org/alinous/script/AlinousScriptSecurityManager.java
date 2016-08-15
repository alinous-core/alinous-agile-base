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

package org.alinous.script;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alinous.AlinousUtils;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.expections.ReferNotAllowedException;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.objects.html.FormTagObject;
import org.alinous.repository.AlinousModule;
import org.alinous.script.basic.ReferFromSentence;
import org.alinous.script.basic.ValidatorSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.validator.ServerValidationRequest;

public class AlinousScriptSecurityManager
{
	private static AlinousScriptSecurityManager instance;
	
	public static AlinousScriptSecurityManager getInstance()
	{
		if(instance == null){
			instance = new AlinousScriptSecurityManager();
		}
		
		return instance;
	}
	
	public void checkSecurity(PostContext context, VariableRepository valRepo, AlinousScript script) throws ExecutionException, RedirectRequestException
	{
		if(!script.getReferFromSentence().isEmpty()){
			serverReffererCheck(context, valRepo, script);
		}
		
		if(!script.getValidatorSentence().isEmpty()){
			
			try {
				serverValidationCheck(context, valRepo, script);
			} catch (AlinousException e) {
				throw new ExecutionException(e, "Failed in server validation."); // i18n
			}
		}
	}
	
	private void serverValidationCheck(PostContext context, VariableRepository valRepo, AlinousScript script) throws AlinousException
	{
		String requesrPath = AlinousUtils.getNotOSPath(context.getUnit().getExecModule().getScript().getFilePath());
		
		ValidatorSentence validatorSentence = script.getValidatorSentence().get(0);
		IStatement stmt = validatorSentence.getArgs().getStatement(0);
		IScriptVariable val = stmt.executeStatement(context, valRepo);
		
		String validationHtmlPath = "";
		if(val instanceof ScriptDomVariable){
			validationHtmlPath = ((ScriptDomVariable)val).getValue();
		}
		
		if(!isAbsolutePath(validationHtmlPath)){
			String home = AlinousUtils.getDirectory(requesrPath);
			validationHtmlPath = AlinousUtils.getAbsoluteNotOSPath(home, validationHtmlPath);
		}
		
		// register module
		String moduleName = AlinousUtils.getModuleName(validationHtmlPath);
		context.getCore().registerAlinousObject(context, moduleName);
		
		AlinousModule mod = context.getCore().getModuleRepository().getModule(moduleName);
		AlinousTopObject obj = mod.getDesign();
		
		// find form
		IStatement stmtFrm = validatorSentence.getArgs().getStatement(1);
		String formName = ((ScriptDomVariable)stmtFrm.executeStatement(context, valRepo)).getValue();
		
		FormTagObject form = getForm(obj, formName);
		
		if(form == null){
			throw new ExecutionException("Form for validation does not exists."); // i18n
		}
		
		List<ServerValidationRequest> validatorList =  gatherValidators(form, formName, context, valRepo);
		
		executeValidator(validatorList, validationHtmlPath + ".html", context, valRepo);
	}
	
	private void executeValidator(List<ServerValidationRequest> validatorList, String validationHtmlPath,
			PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Iterator<ServerValidationRequest> it = validatorList.iterator();
		while(it.hasNext()){
			ServerValidationRequest req = it.next();
			req.executeValidation(context, valRepo, validationHtmlPath);
		}
	}
	
	private List<ServerValidationRequest> gatherValidators(FormTagObject form, String formName,
			PostContext context, VariableRepository valRepo)
	{
		List<ServerValidationRequest> list = new ArrayList<ServerValidationRequest>();
		
		IAttribute attr = form.getAttribute("action");
		if(attr == null){
			return list;
		}
		String frmAction = attr.getValue().getParsedValue(context, valRepo);
		
		Stack<XMLTagBase> stack = new Stack<XMLTagBase>();
		stack.push(form);
		
		while(!stack.isEmpty()){
			XMLTagBase tag = stack.pop();
			
			// get Validator
			List<ServerValidationRequest> reqList = ServerValidationRequest.createRequest(tag, formName, frmAction, context, valRepo);
			if(reqList != null && !reqList.isEmpty()){
				list.addAll(reqList);
			}
			
			// children
			Iterator<XMLTagBase> it = tag.getInnerTags().iterator();
			while(it.hasNext()){
				XMLTagBase chTag = it.next();
				stack.push(chTag);
			}
		}
		
		
		return list;
	}
	
	private FormTagObject getForm(AlinousTopObject obj, String formName)
	{
		Stack<XMLTagBase> stack = new Stack<XMLTagBase>();
		stack.push(obj);
		
		while(!stack.isEmpty()){
			XMLTagBase tag = stack.pop();
			
			// check the tag itself
			if(tag instanceof FormTagObject){
				String name = ((FormTagObject)tag).getName();
				
				if(name != null && name.equals(formName)){
					return (FormTagObject)tag;
				}
			}
			
			// put childern on the top of the stack
			Iterator<XMLTagBase> it = tag.getInnerTags().iterator();
			while(it.hasNext()){
				XMLTagBase chTag = it.next();
				
				stack.push(chTag);
			}
		}
		
		
		return null;
	}
	
	private void serverReffererCheck(PostContext context, VariableRepository valRepo, AlinousScript script) throws ExecutionException, RedirectRequestException
	{
		String requesrPath = AlinousUtils.getNotOSPath(context.getUnit().getExecModule().getScript().getFilePath());
		
		List<String> allowedList = getAllowedReferer(context, valRepo, script.getReferFromSentence(), requesrPath);
		
		// check referer
		String referer = context.getHttpHeaders().get("REFERER");
		
		if(referer == null || referer.equals("")){
			throw new ReferNotAllowedException("Security Error....");
		}
		
		List<String> referers = parseReferer(context, referer);
		
		if(!matchReferer(referers, allowedList)){
			throw new ReferNotAllowedException("Security Error....");
		}
	}
	
	private boolean matchReferer(List<String> referers, List<String> allowedList)
	{
		Iterator<String> it = referers.iterator();
		while(it.hasNext()){
			String ref = it.next();
			
			if(allowedList.contains(ref)){
				return true;
			}
		}
		
		return false;
	}
	
	private List<String> parseReferer(PostContext context, String referer)
	{
		List<String> list = new ArrayList<String>();
		
		Pattern pattern = Pattern.compile("(http|https)://[^/]*/");
		Matcher matcher = pattern.matcher(referer);
		
		if(!matcher.find()){
			return list;
		}
		
		int end = matcher.end();
		
		
		// check host
		String refererHost = referer.substring(0, end);
		String remoteHost = context.getHttpHeaders().get("HOST");
		
		if(refererHost.startsWith("http")){
			remoteHost = "http://" + remoteHost;
		}else{
			remoteHost = "https://" + remoteHost;
		}
		
		if(!refererHost.startsWith(remoteHost)){
			return list;
		}
		
		referer = referer.substring(end - 1, referer.length());
		String strs[] = referer.split("\\?");
		
		if(strs[0].endsWith("/")){
			strs[0] = strs[0] + "index.html";
		}
		
		if(strs[0].indexOf(';') > 0){
			strs[0] = strs[0].split(";")[0];
		}
		
		list.add(strs[0]);
		
		if(strs.length < 2){
			return list;
		}

		// param decode
		String param = strs[1];
		
		String params[] = param.split("&");
		for(int i = 0; i < params.length; i++){
			String keyAndVal[] = params[i].split("=");
			
			if(keyAndVal.length != 2){
				continue;
			}
			
			try {
				String key = URLDecoder.decode(keyAndVal[0], "utf-8");
				String value = URLDecoder.decode(keyAndVal[1], "utf-8");
				
				if(key.equals(FormTagObject.HIDDEN_FORM_ACTION)){
					list.add(value);
				}
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				context.getCore().reportError(e);
			}
		}
		
		return list;
	}
	
	private List<String> getAllowedReferer(PostContext context, VariableRepository valRepo,
			List<ReferFromSentence> list, String requesrPath) throws ExecutionException, RedirectRequestException
	{
		List<String> retList = new ArrayList<String>();
		
		Iterator<ReferFromSentence> it = list.iterator();
		while(it.hasNext()){
			ReferFromSentence sentence = it.next();
			
			IStatement stmt = sentence.getArgs().getStatement(0);
			IScriptVariable val = stmt.executeStatement(context, valRepo);
			
			if(val instanceof ScriptDomVariable){
				//AlinousDebug.debugOut("@@@@@@@@@@@@@@@ arg : " + ((ScriptDomVariable)val).getValue());
			
				String allowedPath =((ScriptDomVariable)val).getValue();
				
				if(!isAbsolutePath(allowedPath)){
					String home = AlinousUtils.getDirectory(requesrPath);
					allowedPath = AlinousUtils.getAbsoluteNotOSPath(home, allowedPath);
				}
				
				retList.add(allowedPath);
			}
		}
		
		return retList;
	}
	
	private boolean isAbsolutePath(String path)
	{
		return path.startsWith("/");
	}
	
}
