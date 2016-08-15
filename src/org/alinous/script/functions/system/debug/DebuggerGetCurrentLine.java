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

public class DebuggerGetCurrentLine extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.GETCURRENTLINE";
	public static final String THREAD_ID = "THREAD_ID";
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	public DebuggerGetCurrentLine()
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
		
		int line = dthread.getTopStackFrame().getLine();
		String modulePath = dthread.getTopStackFrame().getFileName();
		
		ScriptDomVariable lineDom = new ScriptDomVariable("line");
		lineDom.setValue(Integer.toString(line));
		lineDom.setValueType(IScriptVariable.TYPE_NUMBER);
		
		ScriptDomVariable modulePathDom = new ScriptDomVariable("modulePath");
		modulePathDom.setValue(modulePath);
		modulePathDom.setValueType(IScriptVariable.TYPE_STRING);
		
		lineDom.put(modulePathDom);
		
		return lineDom;
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
		return "Debugger.getCurrentLine($threadId)";
	}

	@Override
	public String descriptionString() {
		return "Returns the thread's line and module file's path.";
	}

}
