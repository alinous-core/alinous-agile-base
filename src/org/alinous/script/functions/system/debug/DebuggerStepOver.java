package org.alinous.script.functions.system.debug;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugThread;
import org.alinous.debug.breakstatus.StepOverOperation;
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

public class DebuggerStepOver extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.STEPOVER";
	public static final String THREAD_ID = "THREAD_ID";
	
	public DebuggerStepOver()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", THREAD_ID);
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(THREAD_ID);
		IScriptVariable threadIdVariable = newValRepo.getVariable(ipath, context);
		
		String strThreadId = ((ScriptDomVariable)threadIdVariable).getValue();
		
		long threadId = Long.parseLong(strThreadId);
		
		AlinousDebugManager manager = context.getCore().getAlinousDebugManager();
		
		DebugThread thread = getDebugThread(manager, threadId);
		
		manager.setOperation(threadId, new StepOverOperation(thread.getTopStackFrame().getStackId()));
		manager.resume(threadId, context);
		
		return null;
	}
	
	private DebugThread getDebugThread(AlinousDebugManager manager, long threadId)
	{
		DebugThread[] threads = manager.getThreads();
		for (int i = 0; i < threads.length; i++) {
			if(threads[i].getThreadId() == threadId){
				return threads[i];
			}
		}
		
		return null;
	}
	
	@Override
	public String codeAssistString() {
		return "Debugger.stepOver($threadId)";
	}

	@Override
	public String descriptionString() {
		return "Setp over the line.";
	}

}
