package org.alinous.script.functions.system.debug;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.Iterator;

import org.alinous.AlinousCore;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.AlinousExecutableModule;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.parser.script.AlinousScriptParser;
import org.alinous.parser.script.ParseException;
import org.alinous.script.AlinousScript;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class ScriptDebugEvaluate extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "SCRIPT.DEBUGEVALUATE";
	public static String SCRIPT = "script";
	public static String SCRIPT_MODULE_NAME = "scriptModuleName";
	public static String SCRIPT_PARAMS = "scriptParams";
	public static String CALLBACK_SCRIPT = "callBackScriptFile";
	public static String BREAK_POINTS = "breakPoints";
	
	public ScriptDebugEvaluate()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", SCRIPT);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", SCRIPT_MODULE_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", SCRIPT_PARAMS);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", CALLBACK_SCRIPT);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("@", BREAK_POINTS);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(SCRIPT);
		IScriptVariable scriptVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(SCRIPT_MODULE_NAME);
		IScriptVariable ModuleNameDom = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(SCRIPT_PARAMS);
		ScriptDomVariable paramsDom = (ScriptDomVariable)newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(CALLBACK_SCRIPT);
		IScriptVariable callBackDom = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(BREAK_POINTS);
		ScriptArray breakPointsDom = (ScriptArray) newValRepo.getVariable(ipath, context);
		
		String scriptString = ((ScriptDomVariable)scriptVariable).getValue();
		String callbackScript = ((ScriptDomVariable)callBackDom).getValue();
		
		String moduleName = ((ScriptDomVariable)ModuleNameDom).getValue();
		
		StringReader reader = new StringReader(scriptString);
		AlinousScriptParser parser = new AlinousScriptParser(reader);
		
		setupBreakpoints(context.getCore(), moduleName, breakPointsDom);
		
		long threadId = -1;
		try {
			AlinousScript script = parser.parse();
			
			AccessExecutionUnit newExecutableUnit = context.getCore().createAccessExecutionUnit(callbackScript);
			AlinousExecutableModule module = new AlinousExecutableModule(moduleName, null, script, 0);
			newExecutableUnit.setExecModule(module);
			
			PostContext execContext = new PostContext(context.getCore(), newExecutableUnit);
			execContext.initParams(context);
			
			DebugCallbackHandler debugCallbackHandler = new DebugCallbackHandler(callbackScript, execContext);
			
			execContext.setDebugCallbackHandler(debugCallbackHandler);
			
			VariableRepository scriptVRepo = new VariableRepository();
			
			Iterator<String> it = paramsDom.getPropertiesIterator();
			while (it.hasNext()) {
				String key = it.next();
				
				IScriptVariable v = paramsDom.get(key);
				scriptVRepo.putValue(v, execContext);
			}
			
			ScriptApiRunnable runnable = new ScriptApiRunnable(script, moduleName, context.getCore(), callbackScript,
					execContext, scriptVRepo);
			Thread debugeeThread = new Thread(runnable);
			debugeeThread.start();
			
			threadId = debugeeThread.getId();
		} catch (ParseException e) {
			throw new ExecutionException(e, "The script you inputed is wrong."); // i18n
		} catch (FileNotFoundException e) {
			throw new ExecutionException(e, "The script you inputed is wrong."); // i18n
		}
		
		
		// Return Thread Id
		ScriptDomVariable ret = new ScriptDomVariable("RETURN");
		ret.setValue(Long.toString(threadId));
		ret.setValueType(IScriptVariable.TYPE_NUMBER);
		
		return ret;
	}
	
	private void setupBreakpoints(AlinousCore core, String moduleName, ScriptArray breakPointsDom)
	{
		BreakpointHandler handler = new BreakpointHandler(core.getAlinousDebugManager());
		
		// set break points
		Iterator<IScriptVariable> it = breakPointsDom.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			
			ScriptDomVariable domValue = (ScriptDomVariable)val;
			String intString = domValue.getValue();
			int line = Integer.parseInt(intString);
			
			handler.addBreakpoint(moduleName, line);
		}
	}

	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Script.debugEvaluate($script, $scriptModuleName, $params, $callBackScriptFile)";
	}

	@Override
	public String descriptionString() {
		return "Executes $script string as script. In the script, The variables at calling this function is available." +
				" The callBackScriptFile is executed when the debugger events is happened. This function returns ThreadId.";
	}
	
}
