package org.alinous.script.functions.system.debug;

import org.alinous.AlinousCore;
import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugThread;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.runtime.VariableRepository;

public class ScriptApiRunnable implements Runnable{
	private AlinousScript script;
	private AlinousCore core;
	private String moduleName;
	private PostContext context;
	private VariableRepository valRepo;
	private String callbackScript;
	
	public ScriptApiRunnable(AlinousScript script, String moduleName, AlinousCore core, String callbackScript,
			PostContext context, VariableRepository valRepo)
	{
		this.script = script;
		this.moduleName = moduleName;
		this.core = core;
		this.context = context;
		this.valRepo = valRepo;
		this.callbackScript = callbackScript;
	}
	
	@Override
	public void run()
	{
		this.script.setFilePath(this.moduleName);
		this.script.setDataSourceManager(this.core.getDataSourceManager());
		
		this.context.setDebugMode(true);
		this.context.setCallbackScript(this.callbackScript);
		
		// for debugger
		if(AlinousCore.debug(this.context)){
			this.context.getCore().getAlinousDebugManager().startAlinousOperation(this.context);
			
			AlinousDebugManager mgr = this.context.getCore().getAlinousDebugManager();
			DebugThread th = mgr.getCurrentThread();
			
			th.newStackFrame(this.script,
				this.valRepo, this.context);
		}
		
		try {
			script.execute(context, valRepo, true, true);
		} catch (ExecutionException e) {
			core.getLogger().reportError(e);
			e.printStackTrace();
		} catch (RedirectRequestException e) {
			core.getLogger().reportError(e);
			e.printStackTrace();
		}finally{
			// for debugger
			this.context.getCore().getAlinousDebugManager().endAlinousOperation(this.context);				
			
			this.context.dispose();
		}
	}

}
