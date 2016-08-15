package org.alinous.script.functions.system.debug;

import org.alinous.debug.AlinousDebugManager;
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

public class DebuggerClearAllBreakpoints extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.CLEARALLBREAKPOINTS";
	public static String FILE_NAME = "fileName";
	
	public DebuggerClearAllBreakpoints()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", FILE_NAME);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(FILE_NAME);
		IScriptVariable fileNameVariable = newValRepo.getVariable(ipath, context);
		
		String fileName = ((ScriptDomVariable)fileNameVariable).getValue();
		
		AlinousDebugManager manager = context.getCore().getAlinousDebugManager();
		
		manager.clearBreakpoints(fileName);
		
		return null;
	}
	
	@Override
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Debugger.clearAllBreakpoints($modulePath)";
	}

	@Override
	public String descriptionString() {
		return "Clear all the breakpoints of the file.";
	}
}
