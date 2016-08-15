package org.alinous.script.functions.system.debug;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugThread;
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


public class DebuggerTerminateThread extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.TERMINATETHREAD";
	public static final String THREAD_ID = "THREAD_ID";
	
	public DebuggerTerminateThread()
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
		
		String threadIdStr = ((ScriptDomVariable)threadIdVariable).getValue();
		long threadId = Long.parseLong(threadIdStr);
		
		AlinousDebugManager manager = context.getCore().getAlinousDebugManager();
		
		DebugThread debugThread = null;
		
		DebugThread debugThreads[] = manager.getThreads();
		for(int i = 0; i < debugThreads.length; i++){
			if(debugThreads[i].getThreadId() == threadId){
				debugThread = debugThreads[i];
			}
		}
		
		if(debugThread == null){
			return null;
		}
		
		debugThread.terminate(context);
		
		return null;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Debugger.terminateThread($threadId)";
	}

	@Override
	public String descriptionString() {
		return "Terminate debugging thread.";
	}


}
