/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alinous.script.functions.system;

import java.util.Stack;

import org.alinous.AlinousCore;
import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugThread;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.AlinousExecutableModule;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.VariableDescriptor;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.FuncDeclarations;
import org.alinous.script.functions.IFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class ThreadExecute extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "THREAD.EXECUTE";
	
	public static String EXEC_FUNC_NAME = "execFunctionName";
	public static String EXEC_PARAM = "execParam";
	
	public ThreadExecute()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", EXEC_FUNC_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", EXEC_PARAM);
		this.argmentsDeclare.addArgument(arg);
	}
	
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(EXEC_FUNC_NAME);
		IScriptVariable funcNameVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(EXEC_PARAM);
		IScriptVariable paramsVariable = newValRepo.getVariable(ipath, context);
		
		if(!(funcNameVariable instanceof ScriptDomVariable) ||
				!(paramsVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String funcName = ((ScriptDomVariable)funcNameVariable).getValue();
		
		FuncDeclarations funcDec = context.getUnit().getExecModule().getScript().getFuncDeclarations();
		IFunction func = funcDec.findFunctionDeclare(funcName);
		
		
		// if null load from include
		if(func == null){
			func = context.getIncludeFuncDeclaration(funcName);
		}

		
		PostContext newContext = new PostContext(context.getCore(), context.getUnit().copyUnit());
		newContext.initParams(context);
		newContext.initIncludes(context);
		
		VariableDescriptor desc = new VariableDescriptor("$", ipath);
			
		// Thread execute
		FuncExecRunnable runable = new FuncExecRunnable(func, desc, newContext, newValRepo);
		
		Thread th = new Thread(runable);
		
		th.start();
		
		ScriptDomVariable dom = new ScriptDomVariable("ret");
		dom.setValueType(IScriptVariable.TYPE_NUMBER);
		dom.setValue(Long.toString(th.getId()));
		
		return dom;
	}
	
	protected class FuncExecRunnable implements Runnable{
		private IFunction func;
		private IStatement param;
		private PostContext context;
		private VariableRepository valRepo;
		
		//private 
		public FuncExecRunnable(IFunction func, IStatement param,
				PostContext context, VariableRepository valRepo)
		{
			this.func = func;
			this.param = param;
			this.context = context;
			this.valRepo = valRepo;
		}
		
		public void run()
		{
			long threadId = Thread.currentThread().getId();
			ThreadFuncThreadsPool.getThreadPool().registerThread(Thread.currentThread());
			
			Stack<IStatement> stmtStack = new Stack<IStatement>();
			
			//this.param.setName(name)
			stmtStack.push(this.param);
			
			this.context.pushNewFuncArgStack(stmtStack);
			
			IScriptSentence dummySentence = ThreadExecute.this.getCallerSentence();
			this.func.setCallerSentence(dummySentence);
			
			// for debugger
			if(AlinousCore.debug(this.context)){
				this.context.getCore().getAlinousDebugManager().startAlinousOperation(this.context);
				
				AlinousDebugManager mgr = this.context.getCore().getAlinousDebugManager();
				DebugThread th = mgr.getCurrentThread();
				
				AccessExecutionUnit unit = this.context.getUnit();
				AlinousExecutableModule execModule = unit.getExecModule();

				th.newStackFrame(execModule.getScript(),
					this.valRepo, this.context);
			}
			
			try {
				this.func.executeFunction(this.context, this.valRepo);
			} catch (Throwable e) {
				e.printStackTrace();
				context.getCore().reportError(e);
			}finally{
				// for debugger
				if(AlinousCore.debug(this.context)){
					this.context.getCore().getAlinousDebugManager().endAlinousOperation(this.context);				
				}
				
				this.context.dispose();
				
				ThreadFuncThreadsPool.getThreadPool().releaseThread(threadId);
			}
		}
		
	}
	
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Thread.execute($execFunctionName, $execParam)";
	}

	@Override
	public String descriptionString() {
		return "Execute function with new thread.\n" +
				"The specified function should have a dom variable. The $execParam is to be set into the variable.\n" +
				"ie.\n" +
				"function threadFunc($execParam){.....}";
	}
}
