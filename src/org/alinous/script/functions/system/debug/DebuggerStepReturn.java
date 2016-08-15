package org.alinous.script.functions.system.debug;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugThread;
import org.alinous.debug.breakstatus.StepReturnOperation;
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


public class DebuggerStepReturn extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.STEPRETURN";
	public static final String THREAD_ID = "THREAD_ID";
	
	public DebuggerStepReturn()
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
		
		DebugThread thread = getDebugThread(manager, threadId);
		
		long stackId = -1;
		if(thread.getStackFrames().length >= 2){
			stackId = thread.getStackFrames()[thread.getStackFrames().length - 2].getStackId();
		}
		
		
		manager.setOperation(threadId, new StepReturnOperation(stackId));
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
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	
	@Override
	public String codeAssistString() {
		return "Debugger.stepReturn($threadId)";
	}

	@Override
	public String descriptionString() {
		return "Step return the thread.";
	}

}
