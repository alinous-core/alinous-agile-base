package org.alinous.script.functions.system.debug;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.breakstatus.StepInOperation;
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

public class DebuggerStepIn extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.STEPIN";
	public static final String THREAD_ID = "THREAD_ID";
	
	public DebuggerStepIn()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", THREAD_ID);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(THREAD_ID);
		IScriptVariable threadIdVariable = newValRepo.getVariable(ipath, context);
		
		String strThreadId = ((ScriptDomVariable)threadIdVariable).getValue();
		
		long threadId = Long.parseLong(strThreadId);
		
		AlinousDebugManager manager = context.getCore().getAlinousDebugManager();
		
		StepInOperation ope = new StepInOperation();
		manager.setOperation(threadId, ope);
		manager.resume(threadId, context);
		
		return null;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Debugger.stepIn($threadId, $stackId)";
	}

	@Override
	public String descriptionString() {
		return "Step in the function.";
	}

}
