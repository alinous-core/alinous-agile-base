package org.alinous.stdio;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.PostContext;
import org.alinous.exec.pages.StringParamValue;
import org.alinous.script.runtime.VariableRepository;

public class AlinousModuleStdioListner implements IStdIoListner {
	private String name;
	private String modulePath;
	private AlinousCore core;
	
	public AlinousModuleStdioListner(String name, String modulePath, AlinousCore core)
	{
		this.name = name;
		this.modulePath = AlinousUtils.getModuleName(modulePath);
		this.core = core;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void out(String message) {
		try{
			final String msg = message;
			Thread consolethread = new Thread(new Runnable() {
				@Override
				public void run() {
					doOut(msg);
				}
			});
			consolethread.run();
			
			
		}catch(Throwable e){
			e.printStackTrace();
		}
	}
	
	public void doOut(String message) {
		String sessionId = "NO_SESSION";
		VariableRepository repo = new VariableRepository();
		
		AccessExecutionUnit unit = null;
		
		unit = new AccessExecutionUnit(core.getModuleRepository(), sessionId, core.getSystemRepository(), core.getDataSourceManager(), core);
		unit.setSecurityManager(core.getSecurityManager());
		unit.setConfig(core.getConfig());
		
		PostContext context = new PostContext(core, unit);
		context.setRequestPath(modulePath);
		
		context.getParamMap().put("message", new StringParamValue(message));
		
		try {
			this.core.registerAlinousObject(context, modulePath);
			
			unit.pluginExecute(modulePath, context, repo, true, false);
		} catch (Throwable e) {
			core.reportError(e);
			e.printStackTrace();
		} finally{
			// for debugger
			/*if(AlinousCore.debug(context)){
				AlinousDebugManager mgr = context.getCore().getAlinousDebugManager();
				//context.getCore().getAlinousDebugManager().endAlinousOperation(context);				
			}*/
			
			unit.dispose();
			context.dispose();
		}
		
	
	}

	@Override
	public void dispose() {
		
	}

}
