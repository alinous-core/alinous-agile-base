package org.alinous.script.functions.system.debug;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugStackFrame;
import org.alinous.debug.DebugThread;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class DebuggerGetStacks extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.GETSTACKS";
	public static final String THREAD_ID = "THREAD_ID";
	
	public DebuggerGetStacks()
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
		
		DebugStackFrame[] frames = dthread.getStackFrames();
		ScriptArray retArray = new ScriptArray("candidates");
		
		for(int i = 0; i < frames.length; i++){
			ScriptDomVariable stack = new ScriptDomVariable("stack");
			
			long stackId = frames[i].getStackId();
			String modulePath = frames[i].getFileName();
			int line = frames[i].getLine();
			
			ScriptDomVariable stackIdDom = new ScriptDomVariable("stackId");
			stackIdDom.setValue(Long.toString(stackId));
			stackIdDom.setValueType(IScriptVariable.TYPE_NUMBER);
			
			ScriptDomVariable modulePathDom = new ScriptDomVariable("modulePath");
			modulePathDom.setValue(modulePath);
			modulePathDom.setValueType(IScriptVariable.TYPE_STRING);
			
			ScriptDomVariable lineDom = new ScriptDomVariable("line");
			lineDom.setValue(Integer.toString(line));
			lineDom.setValueType(IScriptVariable.TYPE_NUMBER);
			
			stack.put(stackIdDom);
			stack.put(modulePathDom);
			stack.put(lineDom);
			
			retArray.add(stack);
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
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Debugger.getStacks($threadId)";
	}

	@Override
	public String descriptionString() {
		return "Getting the stackframe of the thread.";
	}
}
