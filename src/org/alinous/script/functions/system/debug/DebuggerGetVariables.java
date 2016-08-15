package org.alinous.script.functions.system.debug;

import java.util.Iterator;

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

public class DebuggerGetVariables extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.GETVARIABLES";
	public static final String THREAD_ID = "THREAD_ID";
	
	public DebuggerGetVariables()
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
		DebugThread dthread = getDebugThread(manager, threadId);
		
		if(dthread == null){
			return null;
		}
		
		VariableRepository stackRepo = dthread.getTopStackFrame().getRepo();
		
		ScriptDomVariable retDom = new ScriptDomVariable("variables");
		
		Iterator<String> it = stackRepo.getKeyIterator();
		while(it.hasNext()){
			String key = it.next();
			
			IScriptVariable val = stackRepo.get(key);
			retDom.put(val);
		}
		
		return retDom;
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
		return "Debugger.getVariables($threadId)";
	}

	@Override
	public String descriptionString() {
		return "Getting the variagbles in the stackframe of the thread.";
	}
}
