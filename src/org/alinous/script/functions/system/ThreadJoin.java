package org.alinous.script.functions.system;

import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class ThreadJoin extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "THREAD.JOIN";
	
	public static String THREAD_ID = "threadId";
	
	public ThreadJoin()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", THREAD_ID);
		this.argmentsDeclare.addArgument(arg);
	}
	
	@Override
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		if(stmtStack.size() != this.argmentsDeclare.getSize()
				&& stmtStack.size() != this.argmentsDeclare.getSize() - 1){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(THREAD_ID);
		IScriptVariable threadIdVariable = newValRepo.getVariable(ipath, context);
		
		if(!(threadIdVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String strThreadId = ((ScriptDomVariable)threadIdVariable).getValue();
		
		long threadId = Long.parseLong(strThreadId);
		
		Thread th = ThreadFuncThreadsPool.getThreadPool().getThread(threadId);
		try {
			if(th != null){
				th.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			context.getCore().getLogger().reportError(e);
		}
		
		return null;
	}
	
	@Override
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Thread.join($threadId)";
	}

	@Override
	public String descriptionString()
	{
		return "Join thread with parent.";
	}

}
