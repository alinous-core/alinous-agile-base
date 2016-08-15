package org.alinous.script.functions.system.debug;

import java.util.Iterator;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugThread;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.IScriptObject;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;

public class DebuggerGetStepInCandidates extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.GETSTEPINCANDIDATES";
	public static final String THREAD_ID = "THREAD_ID";
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	public DebuggerGetStepInCandidates()
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
		
		ScriptArray retArray = new ScriptArray("candidates");
		
		StepInCandidates candidates = dthread.getTopStackFrame().getCurrentCandidate();
		
		if(candidates == null){
			return retArray;
		}
		
		Iterator<IScriptObject> it = candidates.getCandidates().iterator();
		while(it.hasNext()){
			IScriptObject scriptObj = it.next();
			
			if(scriptObj instanceof FunctionCall){
				FunctionCall call = (FunctionCall)scriptObj;
				
				String funcName = call.getFuncName();
				ScriptDomVariable func = new ScriptDomVariable("candidate");
				func.setValue(funcName);
				func.setValueType(IScriptVariable.TYPE_STRING);
				
				retArray.add(func);				
			}
		}
		
		return retArray;
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
		return "Debugger.getStepInCandidates($threadId)";
	}

	@Override
	public String descriptionString() {
		return "Returns array of candidate functions to stepin.";
	}
	
}
