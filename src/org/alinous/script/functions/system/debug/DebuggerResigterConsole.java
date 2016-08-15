package org.alinous.script.functions.system.debug;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.stdio.AlinousModuleStdioListner;

public class DebuggerResigterConsole extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.REGISTERCONSOLE";
	
	public static String NAME = "name";
	public static String MODULE_NAME = "moduleName";
	
	public DebuggerResigterConsole()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", MODULE_NAME);
		this.argmentsDeclare.addArgument(arg);
		
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(NAME);
		IScriptVariable nameVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(MODULE_NAME);
		IScriptVariable moduleNameVariable = newValRepo.getVariable(ipath, context);
		
		String name = ((ScriptDomVariable)nameVariable).getValue();
		String modulePath = ((ScriptDomVariable)moduleNameVariable).getValue();
		
		AlinousModuleStdioListner listner = new AlinousModuleStdioListner(name, modulePath, context.getCore());
		context.getCore().getStdio().addListner(listner);
		
		return null;
	}
	
	@Override
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Debugger.registerConsole($name, $modulePath)";
	}
	
	@Override
	public String descriptionString() {
		return "Register module to accept server message.";
	}

}