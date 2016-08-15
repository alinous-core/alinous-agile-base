package org.alinous.parallel.exam.function;

import java.util.Iterator;
import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.parallel.AlinousThreadScope;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class ParallelJoin extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "PARALLEL.JOIN";
	
	public static String SCOPE_ID = "scopeId";
	
	public ParallelJoin()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", SCOPE_ID);
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(SCOPE_ID);
		IScriptVariable scopeIdVariable = newValRepo.getVariable(ipath, context);
		
		if(!(scopeIdVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		ScriptDomVariable val = (ScriptDomVariable)scopeIdVariable;
		String scopeName = val.getValue();
		
		
		
		AlinousThreadScope scope = getScope(context, scopeName);
		if(scope == null){
			throw new ExecutionException("The coresponding parallel block does not exists");
		}
		
		scope.join(context.getMainThreadContext(scope.toString()));
		
		
		
		return null;
	}
	
	private AlinousThreadScope getScope(PostContext context, String name)
	{
		// 
		//AlinousDebug.debugOutFile("ParallelJoin require sc in context : " + name
		//				, context.getCore().getHome(), "/log/out.txt");
					
		Iterator<AlinousThreadScope> it = context.getParallelExecutedScope().iterator();
		while(it.hasNext()){
			AlinousThreadScope sc = it.next();
			
			if(sc.toString().equals(name)){
				// clear context's scope
				it.remove();
				return sc;
			}
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
		return "Parallel.join($sync)";
	}

	@Override
	public String descriptionString() {
		return "Join with threads executed with the parallel blocks.";
	}
	
}
