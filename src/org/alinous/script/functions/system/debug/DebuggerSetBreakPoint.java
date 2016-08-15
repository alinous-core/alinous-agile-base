package org.alinous.script.functions.system.debug;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.ServerBreakpoint;
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

public class DebuggerSetBreakPoint extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.SETBREAKPOINT";
	public static String FILE_NAME = "fileName";
	public static String LINE_NO = "line";
	
	public DebuggerSetBreakPoint()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", FILE_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", LINE_NO);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(FILE_NAME);
		IScriptVariable fileNameVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(LINE_NO);
		IScriptVariable lineVariable = newValRepo.getVariable(ipath, context);
		
		String fileName = ((ScriptDomVariable)fileNameVariable).getValue();
		String lineNoStr = ((ScriptDomVariable)lineVariable).getValue();
		int line = Integer.parseInt(lineNoStr);
		
		AlinousDebugManager manager = context.getCore().getAlinousDebugManager();
		
		manager.addBreakPoint(new ServerBreakpoint(fileName, line));
		
		return null;
	}
	
	@Override
	public String codeAssistString() {
		return "Debugger.setBreakpoint($fileName, $line)";
	}

	@Override
	public String descriptionString() {
		return "Setting a new breakpoint.";
	}

}
