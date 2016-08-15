package org.alinous.script.basic.parallel;

import org.alinous.AlinousCore;
import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugThread;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.AlinousExecutableModule;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.parallel.AlinousThreadScope;
import org.alinous.parallel.IExecuteParallel;
import org.alinous.script.runtime.VariableRepository;

public class ParallelBlockExec implements IExecuteParallel {
	private AlinousThreadScope scope;
	private boolean executed;
	
	private ParallelBlock paBlock;
	private AlinousCore core;
	
	public ParallelBlockExec(ParallelBlock paBlock)
	{
		this.paBlock = paBlock;
	}
	
	@Override
	public void execute(Object[] params) {
		PostContext context = (PostContext) params[0];
		VariableRepository valRepo = (VariableRepository) params[1];
		
		this.core = context.getCore();
		
		// for debugger
		if(AlinousCore.debug(context)){
			try{
				context.getCore().getAlinousDebugManager().startAlinousOperation(context);
				
				AlinousDebugManager mgr = context.getCore().getAlinousDebugManager();
				DebugThread th = mgr.getCurrentThread();
				
				AccessExecutionUnit unit = context.getUnit();
				AlinousExecutableModule execModule = unit.getExecModule();
	
				th.newStackFrame(execModule.getScript(), valRepo, context);
			}catch(Throwable ignore){
				ignore.printStackTrace();
			}
		}
		
		try {
			//  
		//	AlinousTimeWatcher timer = new AlinousTimeWatcher();
			//AlinousDebug.debugOut("ParallelBlockExec#execute(Object[] params) : " + context);
			
			paBlock.newThreadExecute(context, valRepo);
			
			//timer.stop("Main exec");
		} catch (ExecutionException e) {
			handleError(e);
			e.printStackTrace();
		} catch (RedirectRequestException e) {
			handleError(e);
			e.printStackTrace();
		} catch(Throwable e){
			handleError(e);
			e.printStackTrace();
		}
		finally{
			// for debugger
			if(AlinousCore.debug(context)){
				context.getCore().getAlinousDebugManager().endAlinousOperation(context);				
			}
			
			valRepo.clearFinalRepository(context);
			
			context.dispose(); //　created newContext at ParallelBlock#doExecute()
		}
		
		
		executed = true;
	}
	

	
	@Override
	public void setScope(AlinousThreadScope scope) {
		this.scope = scope;
	}

	@Override
	public AlinousThreadScope getScope() {
		return this.scope;
	}



	@Override
	public void handleError(Throwable e) {
		e.printStackTrace();
		core.getLogger().reportError(e);
	}

	@Override
	public boolean isExecuted() {
		return executed;
	}
}
