package org.alinous.plugin.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugThread;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.AlinousExecutableModule;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.exec.pages.StringParamValue;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.ModuleNotFoundException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class PluginCall extends AbstractSystemFunction {
	public static final String QUALIFIED_NAME = "PLUGIN.CALL";
	
	public static final String TEMPLETE_PATH = "templetePath";
	public static final String HTTP_PARAMS = "httpParams";
	
	public PluginCall()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", TEMPLETE_PATH);
		this.argmentsDeclare.addArgument(arg);
		arg = new ArgumentDeclare("$", HTTP_PARAMS);
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(TEMPLETE_PATH);
		IScriptVariable htmlStringVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(HTTP_PARAMS);
		IScriptVariable httpParamsVariable = newValRepo.getVariable(ipath, context);
		
		if(!(htmlStringVariable instanceof ScriptDomVariable) ||
				!(httpParamsVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String tmpletePath = ((ScriptDomVariable)htmlStringVariable).getValue();
		String tmpleteModulePath = AlinousUtils.getModuleName(tmpletePath);
		
		ScriptDomVariable paramDom = (ScriptDomVariable)httpParamsVariable;
		Map<String, IParamValue> paramMap = getParams(paramDom);
		
		AccessExecutionUnit exec = null;
		
		exec = context.getCore().createAccessExecutionUnit(context.getSessionId(), context.getUnit());
		
		PostContext newContext = new PostContext(context.getCore(), exec);
		newContext.setContextPath(context.getContextPath());
		newContext.setServletPath(context.getServletPath());
		
		newContext.setRequestPath(tmpleteModulePath);
		
		IScriptVariable returned = null;
		DebugThread th = null;
		try {
			// for debugger
			if(AlinousCore.debug(context)){
				//context.getCore().getAlinousDebugManager().startAlinousOperation();
				
				AlinousDebugManager mgr = context.getCore().getAlinousDebugManager();
				th = mgr.getCurrentThread();
				
				AccessExecutionUnit unit = context.getUnit();
				AlinousExecutableModule execModule = unit.getExecModule();
				
				th.newStackFrame(execModule.getScript(),
					valRepo, context);
			}
			
			context.getCore().registerAlinousObject(newContext, tmpleteModulePath);
			
			// params
			newContext.initParams(tmpleteModulePath, paramMap);
			
			// exchange
			//exec.gotoPage(tmpleteModulePath, newContext, newValRepo);
			returned = exec.pluginExecute(tmpleteModulePath, newContext, newValRepo, true, true);
			
		} catch (ModuleNotFoundException e) {
			// ignore
		}
		catch (AlinousException e) {
			context.getCore().reportError(e);
		}finally{
			// for debugger
			if(AlinousCore.debug(context)){
				//context.getCore().getAlinousDebugManager().endAlinousOperation();
				th.destroyStackFrame();
			}
			
			exec.dispose();
			newContext.dispose();
		}
		
		return returned;
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
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Plugin.call($templetePath, $httpParams)";
	}

	@Override
	public String descriptionString() {
		return "Execute $templetePath with $httpParams. " +
				"On executing, $httpParams corresponding with $IN";
	}
}
