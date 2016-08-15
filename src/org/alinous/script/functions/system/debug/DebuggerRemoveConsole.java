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

public class DebuggerRemoveConsole extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.REMOVECONSOLE";
	
	public static String NAME = "name";
	
	public DebuggerRemoveConsole()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", NAME);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(NAME);
		IScriptVariable nameVariable = newValRepo.getVariable(ipath, context);
		
		String name = ((ScriptDomVariable)nameVariable).getValue();
		
		
		context.getCore().getStdio().removeListner(name);
		
		return null;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Debugger.removeConsole($name)";
	}

	@Override
	public String descriptionString() {
		return "Remove registered console";
	}

}
