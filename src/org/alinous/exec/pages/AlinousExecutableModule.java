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
package org.alinous.exec.pages;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.alinous.AlinousConfig;
import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.datasrc.AlinousDataSourceManager;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.ExecResultCache;
import org.alinous.exec.FormValueCache;
import org.alinous.exec.InnerModulePath;
import org.alinous.exec.validator.CustomValidator;
import org.alinous.exec.validator.IValidator;
import org.alinous.exec.validator.ValidationRequest;
import org.alinous.exec.validator.ValidationStatus;
import org.alinous.expections.AlinousException;
import org.alinous.expections.DirectOutputException;
import org.alinous.expections.DownloadException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.repository.AlinousModule;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.DownloadSentence;
import org.alinous.script.basic.IncludeSentence;
import org.alinous.script.basic.RedirectSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.StringConst;
import org.alinous.script.basic.type.VariableDescriptor;
import org.alinous.script.functions.FunctionDeclaration;
import org.alinous.script.functions.IFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FuncArguments;

public class AlinousExecutableModule extends AlinousModule
{
	//private VariableRepository valRepo;
	
	public AlinousExecutableModule(String path, AlinousTopObject ds,
					AlinousScript sc, long lastModified)
	{
		super(path, ds, sc, lastModified);
	}

	public AlinousExecutableModule(AlinousTopObject design, AlinousScript script)
	{
		this.design = design;
		this.script = script;
	}

	public ForwardResult post(PostContext context, VariableRepository valRepo, ExecResultCache resCache,
			AlinousDataSourceManager dataSourceManager, AlinousConfig config)
				throws AlinousException
	{
		// if design is not null and static
		if(this.design != null && !this.design.isDynamic()){
			
		}
		
		// valRepo
		//valRepo = new VariableRepository();

		// init params
		if(!context.isUseValuavleRepositoryCache()){
			//	setup Params in the VariableRepository
			setupVariableRepository(context, valRepo, null);
			
		}
		
		// first, execute script
		if(this.script != null && !context.isUseValuavleRepositoryCache()){
			// setup Variable
			this.script.setDataSourceManager(dataSourceManager);
			
			context.getUnit().getSessionController().updateSession(valRepo, context);
			
			this.script.execute(context, valRepo);
			
			context.getUnit().getSessionController().storeSession(context, valRepo);
			
		}else{
			// load last result from cache
			if(design != null){
				resCache.getResultCache(context, valRepo, context.getModulePath(), this.design.getPath(), context.getSessionId());
				context.getUnit().getSessionController().updateSession(valRepo, context);
			}
			
		}
		
		// handle download
		handleDownload(context, valRepo);
		handleRedirect(context, valRepo);
		
		//  if design is null
		if(this.design == null){
			// target
			//IStatement statement 
			String targetPath = ((ScriptDomVariable)context.getScriptReturnedValue()).getValue();
			
			throw new DirectOutputException(targetPath);
		}
		
		
		// Load FormValue Cache
		if(context.isUseFormCache()){
			loadFormCache(context, valRepo);
		}
		
		// edit validation info
		editValidationInfo(context, valRepo);
		
		//  DEBUG: print
		//AlinousDebug.dumpValues(valRepo);

		
		// redirect and dl
		
		
		
		// forward
		ForwardResult result = handleForwardReturn(context, valRepo);
		if(result.isForward()){
			return result;
		}
		

		
		// get design
		if(this.design != null){
			this.design.post(context, resCache, context.getUnit().getFormValueCache(), valRepo);
		}
		
		// if not forwarded or redirected. Save variables
		// After executed correctly, store result
		if(!context.isUseValuavleRepositoryCache()){
			resCache.storeResult(context, valRepo, context.getModulePath(), this.design.getPath(), context.getSessionId());
		}
		
		return result;
	}
	
	public void pluginPost(PostContext context, VariableRepository valRepo, AlinousDataSourceManager dataSourceManager,
			boolean parepareDebug, boolean initOperation) throws AlinousException
	{
		//	setup Params in the VariableRepository
		setupVariableRepository(context, valRepo, null);
		
		// setup Variable
		this.script.setDataSourceManager(dataSourceManager);
		
		//context.getUnit().getSessionController().updateSession(valRepo, context);
		
		this.script.execute(context, valRepo, parepareDebug, initOperation);
	}
	
	
	private void handleRedirect(PostContext context, VariableRepository valRepo) throws RedirectRequestException
	{
		// Redirect
		IScriptSentence lastStatemtne = context.getLastSentence();
				
		if(lastStatemtne instanceof RedirectSentence){
			String targetPath = ((ScriptDomVariable)context.getScriptReturnedValue()).getValue();
			
			ScriptDomVariable code = (ScriptDomVariable) ((ScriptDomVariable)context.getScriptReturnedValue()).get("redirectCode");
			String strCode = code.getValue();
			
			RedirectRequestException ex = new RedirectRequestException(context.getFilePath(targetPath), strCode);
			
			throw ex;
		}
	}
	
	private void handleDownload(PostContext context, VariableRepository valRepo) throws DownloadException
	{
		// download
		IScriptSentence lastStatemtne = context.getLastSentence();
		
		if(lastStatemtne instanceof DownloadSentence){
			String targetPath = ((ScriptDomVariable)context.getScriptReturnedValue()).getValue();
			String contentType = null;
			String downloadFileName = null;
			
			if(((DownloadSentence)lastStatemtne).getDownloadFileName() != null){
				downloadFileName = ((ScriptDomVariable)((DownloadSentence)lastStatemtne).getDownloadFileNameValue(context)).getValue();
			}
			
			if(((DownloadSentence)lastStatemtne).getContentTypeValue(context) != null){
				contentType = ((ScriptDomVariable)((DownloadSentence)lastStatemtne).getContentTypeValue(context)).getValue();
			}
			
			DownloadException ex = new DownloadException(targetPath);
			ex.setContentType(contentType);
			ex.setFileName(downloadFileName);
			
			throw ex;
			
		}
	}
	
	private ForwardResult handleForwardReturn(PostContext context, VariableRepository valRepo)
		throws AlinousException
	{
		if(!context.isForwarded()){
			return new ForwardResult(false, context);
		}

		String targetPath = ((ScriptDomVariable)context.getScriptReturnedValue()).getValue();
		
		if(targetPath == null){
			return new ForwardResult(false, context);
		}
		
		// target(forward)
		handleExraUrl(targetPath);
		
		if(targetPath.endsWith("/")){
			targetPath = targetPath + "index.html";
		}
		
		ForwardResult result = null;
		AccessExecutionUnit accessUnit = null;
		String moduleName = null;
		PostContext newContext = null;
		try{
			accessUnit = context.getCore().createAccessExecutionUnit(context.getSessionId());
			newContext = new PostContext(context.getCore(), accessUnit);
			
			// static
			newContext.setStatic(context.isStatic());
			
			// module path
			newContext.setValidationStatus(context.getValidationStatus());
			newContext.setModulePath(context.getModulePath().deepClone());
			newContext.setTargetTagId(context.getTargetTagId());
			
			
			result = new ForwardResult(true, newContext);
			
			// Servlet Context
			newContext.setContextPath(context.getContextPath());
			newContext.setServletPath(context.getServletPath());
			
			String osPath = AlinousUtils.getOSPath(targetPath);
			moduleName = AlinousUtils.getModuleName(osPath);
			
			// setparams
			Map<String, IParamValue> params = getParamsFromUrl(targetPath);
			newContext.initParams(moduleName, params);
			
			// header
			newContext.initHttpHeaders(context.getHttpHeaders());
			
			// Check if it is inner or top
			/*
			String nextAction = context.getNextAction();
			if(nextAction == null){
				newContext.setNextAction(moduleName);
				newContext.setTargetTagId(context.getTargetTagId());
				moduleName = context.getRequestPath();
			}
			*/
			
			context.getCore().registerAlinousObject(newContext, moduleName);
			
			newContext.setRequestPath(moduleName);
			
			newContext.setInner(context.isInner());
			
			this.design = (AlinousTopObject) accessUnit.gotoPage(moduleName, newContext, valRepo);
			
			// change valrepo for new execution
			//valRepo = accessUnit.getVariableRepository();
		}
		finally{
			//	does not dispose when forward
			//if(accessUnit != null){
			//	accessUnit.dispose();
			//}
			if(newContext != null){
				newContext.disposeCurrentDataSource();
			}
		}
		
		
		// inner
		if(context.isInner()){
			newContext.setInner(true);
			newContext.setTopTopObject(context.getTopTopObject());
			newContext.setModulePath(context.getModulePath());
			
			// debug
			String filePath = AlinousUtils.getNotOSPath(moduleName);

			if(!accessUnit.getForwardResult().isForward()){
				accessUnit.getInnserStatusCache().storeLastPath(context, newContext.getModulePath(), filePath, newContext.getSessionId());
			}
		}
		
		return result;
		
	}
	
	private Map<String, IParamValue> getParamsFromUrl(String url) throws AlinousException
	{
		Map<String, IParamValue> map = new HashMap<String, IParamValue>();
		
		String pathes[] = url.split("\\?");
		if(pathes.length != 2){
			return map;
		}
		
		//AlinousDebug.debugOut("@@@@@@@@@ pathes[] : " + pathes[1]);
		
		String strParams = pathes[1];
		
		String paramArray[] = strParams.split("&");
		for(int i = 0; i < paramArray.length; i++){
			String nameAndVal[] = paramArray[i].split("=");
			if(nameAndVal.length == 2){
				IParamValue val = null;
				if(nameAndVal[0].endsWith("[]")){
					val = new ArrayParamValue(nameAndVal[1]);
				}
				else{
					val = new StringParamValue(nameAndVal[1]);
				}
				
				map.put(nameAndVal[0], val);
			}
		}
		
		
		return map;
	}
	
	private void handleExraUrl(String targetPath)
				throws RedirectRequestException
	{
		if(targetPath.startsWith("http://") || targetPath.startsWith("https://")){
			RedirectRequestException ex = new RedirectRequestException(targetPath, "302");
			
			throw ex;
		}
	}
	
	
	
	private void editValidationInfo(PostContext context, VariableRepository valRepo)
			throws AlinousException
	{
		// Edit validation information
		valRepo.release(IValidator.VARIABLE_NAME, context);
		valRepo.putValue(IValidator.VARIABLE_NAME, "true", IScriptVariable.TYPE_BOOLEAN, context);
		
		ValidationStatus status = context.getValidationStatus();
		
		if(status == null || status.getStatus()){
			return;
		}
		
		Iterator<ValidationRequest> it = status.iterator();
		while(it.hasNext()){
			ValidationRequest req = it.next();
			
			// Custom validator
			if(req.isCustom()){
				handleCustomValidatorRequest(req, context, valRepo);
				continue;
			}
			
			
			StringBuffer valueName = new StringBuffer();
			valueName.append(IValidator.VARIABLE_NAME);
			
			if(req.getFormName() != null && !req.getFormName().equals("")){
				valueName.append(".");
				valueName.append(req.getFormName());
				
			}
			
			if(req.getInputName() != null && !req.getInputName().equals("")){
				valueName.append(".");
				
				String inputStr = req.getInputName();				
				if(inputStr.endsWith("[]")){
					inputStr = inputStr.substring(0, inputStr.length() -2);
				}

				valueName.append(inputStr);
			}
			
			valueName.append(".");
			valueName.append(req.getValidatorName());
			
		
			valRepo.putValue(valueName.toString(), "true", IScriptVariable.TYPE_BOOLEAN, context);
			valRepo.putValue(IValidator.VARIABLE_NAME, "false", IScriptVariable.TYPE_BOOLEAN, context);
		}
	}
	
	private void handleCustomValidatorRequest(ValidationRequest req,
			PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		StringBuffer base = new StringBuffer();
		base.append(IValidator.VARIABLE_NAME);
		
		if(req.getFormName() != null && !req.getFormName().equals("")){
			base.append(".");
			base.append(req.getFormName());
		}
		
		if(req.getInputName() != null && !req.getInputName().equals("")){
			base.append(".");
			base.append(req.getInputName());
		}
		
		//
		CustomValidator customValidator = (CustomValidator)req.getValidator();
		
		Iterator<String> it = customValidator.getReasons().iterator();
		while(it.hasNext()){
			String reason = it.next();
			
			StringBuffer valueName = new StringBuffer(base.toString());
			
			valueName.append(".");
			valueName.append(reason);
			
			valRepo.putValue(valueName.toString(), "true", IScriptVariable.TYPE_BOOLEAN, context);
			
			valRepo.putValue(IValidator.VARIABLE_NAME, "false", IScriptVariable.TYPE_BOOLEAN, context);
		}
	}
	
	
	private void loadFormCache(PostContext context, VariableRepository valRepo) throws AlinousException
	{
		FormValueCache cache = context.getUnit().getFormValueCache();
		
		FormValues formValues = cache.loadFormValues(context, context.getModulePath(), this.design.getPath(), context.getSessionId());
		context.setFormValues(formValues);
	}
	
	/**
	 * 
	 * @param context
	 * @param valueRepo
	 * @param inputName
	 * @return
	 * @throws ExecutionException
	 * @throws RedirectRequestException
	 */
	public static boolean setupVariableRepository(PostContext context, VariableRepository valueRepo,
			String inputName) throws ExecutionException, RedirectRequestException
	{
		// Is array or not
		boolean isArray = false;
		
		// Params info
		ScriptDomVariable inVariable = new ScriptDomVariable("IN");
		
		List<String> ignoreBlankList = context.getParamsToIgnoreBlank();
		
		Iterator<String> it = context.paramsIterator();
		while(it.hasNext()){
			String key = it.next();
			
			IParamValue value = context.getParams(key);
			
			if(ignoreBlankList.contains(key) && value.getType() == IParamValue.TYPE_STRING){
				
				if(value.toString().equals("")){
					continue;
				}
			}
			
			switch(value.getType()){
			case IParamValue.TYPE_STRING:
				// int params
				ScriptDomVariable param = new ScriptDomVariable(key);
				param.setValue(value.toString());
				// toInteger
				param.setValueType(AlinousUtils.getParamType(key, value.toString(), context.getParamMap()));
				
				inVariable.put(param);
				break;
			case IParamValue.TYPE_ARRAY:
				setupArrayParam(key, inVariable, (ArrayParamValue)value, context);
				
				if(inputName != null && inputName.equals(key)){
					isArray = true;
				}
				
				break;
				
			default:
				break;
			}

		}
		
		if(valueRepo != null){
			valueRepo.putValue(inVariable, context);
		}
		
		// Http Access Info
		ScriptDomVariable httpHeaders = new ScriptDomVariable("HTTP");
		ScriptDomVariable header = new ScriptDomVariable("HEADER");
		httpHeaders.put(header);
		
		// HTTP RESPONSE
		ScriptDomVariable response = new ScriptDomVariable("RESPONSE");
		httpHeaders.put(response);
		
		Map<String, String> map = context.getHttpHeaders();
		it = map.keySet().iterator();
		while(it.hasNext()){
			String name = it.next();
			ScriptDomVariable param = new ScriptDomVariable(name);
			param.setValue(map.get(name));
			
			header.put(param);
		}
		
		ScriptDomVariable param = new ScriptDomVariable("QUERY_STRING");
		param.setValue(context.getQueryString());
		header.put(param);
		
		if(valueRepo != null){
			valueRepo.putValue(httpHeaders, context);
		}
		
		// FIXME COOKIE
		//AlinousDebug.debugOut(map.get("COOKIE"));
		if(valueRepo != null){
			handleCookie(valueRepo, context, map.get("COOKIE"));
		}
		
		return isArray;
		
	}
	
	private static void handleCookie(VariableRepository valueRepo, PostContext context, String cookieString) throws ExecutionException, RedirectRequestException
	{
		if(cookieString == null){
			return;
		}
		
		ScriptDomVariable httpHeaders = new ScriptDomVariable("COOKIE");
		valueRepo.putValue(httpHeaders, context);
		
		String values[] = cookieString.split(";");
		for(int i = 0; i < values.length; i++){
			String base = values[i].trim();
			
			String keyValue[] = base.split("=");
			
			String key = keyValue[0];
			String value = "";
			if(keyValue.length >= 2){
				if(keyValue[1].startsWith("\"") && keyValue[1].endsWith("\"")){
					value = keyValue[1].substring(1, keyValue[1].length() - 1);
				}else{
					value = keyValue[1];
				}
			}
			
			ScriptDomVariable tmpDom = new ScriptDomVariable(key);
			tmpDom.setValueType(IScriptVariable.TYPE_STRING);
			tmpDom.setValue(value);
			
			httpHeaders.put(tmpDom);
		}
	}

	private static void setupArrayParam(String key, ScriptDomVariable inVariable, ArrayParamValue arrayParam,
			PostContext context)
	{
		ScriptArray arrayVariable = new ScriptArray(key);
		inVariable.put(arrayVariable);
		
		Iterator<String> it = arrayParam.getIterator();
		while(it.hasNext()){
			String value = it.next();
			
			ScriptDomVariable vl = new ScriptDomVariable("");
			vl.setValue(value);
			
			// for int and doubel params
			vl.setValueType(AlinousUtils.getParamType(key, value, context.getParamMap()));
			
			arrayVariable.add(vl);
		}
		
	}
	
	public IScriptVariable executeValidation(PostContext context, VariableRepository valRepo, String inputName, 
			String formName, boolean isArray)
					throws AlinousException
	{
		//VariableRepository valRepo = new VariableRepository(); don't make
		
		// setup Params in the VariableRepository
		setupVariableRepository(context, valRepo, inputName);
		
		FunctionDeclaration funcDec = null;
		if(isArray){
			if(inputName.endsWith("[]")){
				inputName = inputName.substring(0, inputName.length() - "[]".length());
			}
			funcDec = script.getFuncDeclarations().findFunctionDeclare("validateArray");
		}else{
			funcDec = script.getFuncDeclarations().findFunctionDeclare("validate");
		}
		
		if(funcDec == null){
			return null;
		}
		
		// init includes
		Iterator<IncludeSentence> it = this.script.includeSentencesIterator();
		while(it.hasNext()){
			IncludeSentence sentence = it.next();
			sentence.execute(context, valRepo);
		}
		
		// exec function
		FuncArguments args = new FuncArguments(); // Runtime Arguments
		StringConst formNameArg = new StringConst();
		formNameArg.setStr(formName);
		StringConst inputNameArg = new StringConst();
		inputNameArg.setStr(inputName);
		
		IStatement valueStmt = variableDesc("IN." + inputName);
		IStatement inStmt = variableDesc("IN");
		IStatement sessionStmt = variableDesc("SESSION");
		
		args.addArgument(formNameArg);
		args.addArgument(inputNameArg);
		args.addArgument(valueStmt);
		args.addArgument(inStmt);
		args.addArgument(sessionStmt);
		
		context.getUnit().getSessionController().updateSession(valRepo, context);
		
		IScriptVariable valRes = executeSourceFunc(funcDec, context, valRepo, args);
		
		
		context.getUnit().getSessionController().storeSession(context, valRepo);
		
		return valRes;
	}
	
	private VariableDescriptor variableDesc(String domPath)
	{
		IPathElement pathEl = PathElementFactory.buildPathElement(domPath);
		
		return new VariableDescriptor("$", pathEl);
	}
	
	private IScriptVariable executeSourceFunc(FunctionDeclaration func, 
						PostContext context, VariableRepository valRepo, FuncArguments args)
		throws ExecutionException, RedirectRequestException
	{
		func.setDataSourceManager(context.getCore().getDataSourceManager());
		
		handleRuntimeArguments(func, args, context);
		
		func.setCallerSentence(func);
		
		// DEBUG: create stack frame
		if(AlinousCore.debug(context)){
			context.getCore().getAlinousDebugManager().createStackFrame(func, valRepo, context);
		}
		
		// return
		func.execute(context, valRepo);
		
		context.popFuncArgStack();
		
		// DEBUG: destory stack frame
		if(AlinousCore.debug(context)){
			context.getCore().getAlinousDebugManager().destoryCurrentStackFrame();
		}
		
		return func.getReturnedVariable(context);
	}
	
	private void handleRuntimeArguments(IFunction func, FuncArguments args, PostContext context)
	{
		if(args == null){
			return;
		}
		
		Stack<IStatement> stmtStack = new Stack<IStatement>();
		Iterator<IStatement> it = args.iterator();
		while(it.hasNext()){
			IStatement stmt = it.next();
			
			stmtStack.push(stmt);
		}
		
		func.inputArguments(stmtStack, context);
	}
	
	
	
	public void setCurrentInnerModulePath(PostContext context, InnerModulePath currentInnerModulePath)
	{
		if(this.design != null){
			context.setModulePath(currentInnerModulePath);
		}
	}

}
